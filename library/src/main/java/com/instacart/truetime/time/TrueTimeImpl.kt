package com.instacart.truetime.time

import com.instacart.truetime.NoOpEventListener
import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.sntp.Sntp
import com.instacart.truetime.sntp.SntpImpl
import com.instacart.truetime.sntp.SntpResult
import com.instacart.truetime.time.TrueTimeParameters.Builder
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

class TrueTimeImpl(
    private val params: TrueTimeParameters = Builder().buildParams(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val listener: TrueTimeEventListener = NoOpEventListener,
    private val sntp: Sntp = SntpImpl(),
) : TrueTime {

  private val timeKeeper = TimeKeeper(sntp, listener)

  private val scope =
      CoroutineScope(
          SupervisorJob() +
              dispatcher +
              CoroutineExceptionHandler { _, throwable ->
                listener.syncDispatcherException(throwable)
              })

  override fun sync(): Job {
    return scope.launch(CoroutineName("TrueTime-Syncer")) {
      while (true) {
        try {
          initialize(params)
        } catch (e: Exception) {
          listener.initializeFailed(e)
        }

        listener.nextInitializeIn(delayInMillis = params.syncIntervalInMillis)
        delay(params.syncIntervalInMillis)
      }
    }
  }

  override fun now() = if (params.returnSafelyWhenUninitialized) nowSafely() else nowTrueOnly()

  override fun nowSafely() = timeKeeper.nowSafely()

  override fun nowTrueOnly() = timeKeeper.nowTrueOnly()

  override fun hasTheTime() = timeKeeper.hasTheTime()

  // region private helpers

  /** Initialize TrueTime with an ntp pool server address */
  private suspend fun initialize(params: TrueTimeParameters): LongArray {
    listener.initialize(params)

    // resolve NTP pool -> single IPs
    val resolvedIPs = resolveNtpHostToIPs(params.ntpHostPool.first())

    val ntpResult: SntpResult =
        if (this.params.strictNtpMode) {
          // for each IP resolved
          resolvedIPs
              .map { ipHost ->
                // 5 times against each IP
                (1..5)
                    .map { requestTime(params, ipHost) }
                    // collect the 5 results to list
                    .toList()
                    // filter least round trip delay to get single Result
                    .filterLeastRoundTripDelay()
              }
              // collect 5 of the results made so far to any of the IPs
              .take(5)
              // filter median clock offset to get single Result
              .filterMedianClockOffset()
        } else {
          coroutineScope {
            select {
                  resolvedIPs.forEach { ipHost ->
                    async { requestTime(params, ipHost) }.onAwait { it }
                  }
                }
                .also { coroutineContext.cancelChildren() }
          }
        }

    listener.initializeSuccess(ntpResult)

    timeKeeper.save(ntpResult = ntpResult)

    return ntpResult
  }

  /** resolve ntp host pool address to single IPs */
  @Throws(UnknownHostException::class)
  private fun resolveNtpHostToIPs(ntpHostAddress: String): List<InetAddress> {
    val ipList: List<InetAddress> = InetAddress.getAllByName(ntpHostAddress).toList()
    listener.resolvedNtpHostToIPs(ntpHostAddress, ipList)

    return ipList.filter { if (params.filterIpv6Addresses) it !is Inet6Address else true }
  }

  private fun requestTime(
      with: TrueTimeParameters,
      ipHostAddress: InetAddress,
  ): SntpResult {
    // retrying up to (default 50) times if necessary
    repeat(with.retryCountAgainstSingleIp - 1) {
      try {
        // request Time
        return sntpRequest(with, ipHostAddress)
      } catch (e: Exception) {
        listener.sntpRequestFailed(e)
      }
    }

    // last attempt
    listener.lastSntpRequestAttempt(ipHostAddress)
    return sntpRequest(with, ipHostAddress)
  }

  private fun sntpRequest(
      with: TrueTimeParameters,
      ipHostAddress: InetAddress,
  ): SntpResult =
      sntp.requestTime(
          ntpHostAddress = ipHostAddress,
          rootDelayMax = with.rootDelayMax,
          rootDispersionMax = with.rootDispersionMax,
          serverResponseDelayMax = with.serverResponseDelayMaxInMillis,
          timeoutInMillis = with.connectionTimeoutInMillis,
          listener = listener,
      )

  private fun List<SntpResult>.filterLeastRoundTripDelay(): SntpResult {
    return minByOrNull { it.roundTripDelay() }
        ?: throw IllegalStateException("Could not find any results from requestingTime")
  }

  private fun List<SntpResult>.filterMedianClockOffset(): SntpResult {
    val sortedList = this.sortedBy { it.clockOffset() }
    return sortedList[sortedList.size / 2]
  }

  // endregion
}

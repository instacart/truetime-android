package com.instacart.truetime.time

import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.NoOpEventListener
import com.instacart.truetime.sntp.Sntp
import com.instacart.truetime.sntp.SntpImpl
import com.instacart.truetime.time.TrueTimeParameters.Builder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Date
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TrueTimeImpl(
    private val params: TrueTimeParameters = Builder().buildParams(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val listener: TrueTimeEventListener = NoOpEventListener,
) : TrueTime {

    private val sntp: Sntp = SntpImpl()
    private val timeKeeper = TimeKeeper(sntp, listener)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher +
      CoroutineExceptionHandler { _, throwable -> listener.syncDispatcherException(throwable) })

    override fun sync(): Job {
        return scope.launch {
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

    override fun hasTheTime(): Boolean = timeKeeper.hasTheTime()

    override fun now(): Date {
        return if (params.shouldReturnSafely) nowSafely() else nowTrueOnly()
    }

    override fun nowSafely(): Date {
          return if (timeKeeper.hasTheTime()) {
              nowTrueOnly()
          } else {
              listener.returningDeviceTime()
              Date()
          }
    }

    override fun nowTrueOnly(): Date {
        if (!hasTheTime()) throw IllegalStateException("TrueTime was not initialized successfully yet")
        return timeKeeper.now()
    }

    //region private helpers

    /**
     * Initialize TrueTime with an ntp pool server address
     */
    private fun initialize(with: TrueTimeParameters): LongArray {
        listener.initialize(with)

        // resolve NTP pool -> single IPs
        val ntpResult = resolveNtpHostToIPs(with.ntpHostPool.first())
            // for each IP resolved
            .map { ipHost ->
                // 5 times against each IP
                (1..5)
                    .map { requestTime(with, ipHost) }
                    // collect the 5 results to list
                    .toList()
                    // filter least round trip delay to get single Result
                    .filterLeastRoundTripDelay()
            }
            // collect max 5 of the IPs in a list
            .take(5)
            // filter median clock offset to get single Result
            .filterMedianClockOffset()

        listener.initializeSuccess(ntpResult)

        timeKeeper.save(ntpResult = ntpResult)

        return ntpResult
    }

    /**
     * resolve ntp host pool address to single IPs
     */
    @Throws(UnknownHostException::class)
    private fun resolveNtpHostToIPs(ntpHostAddress: String): List<String> {
        val ipList = InetAddress.getAllByName(ntpHostAddress).map { it.hostAddress }
        listener.resolvedNtpHostToIPs(ntpHostAddress, ipList)
        return ipList
    }

    private fun requestTime(
      with: TrueTimeParameters,
      ipHostAddress: String,
    ): LongArray {
        // retrying upto (default 50) times if necessary
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
      ipHostAddress: String,
    ): LongArray = sntp.requestTime(
        ntpHostAddress = ipHostAddress,
        rootDelayMax = with.rootDelayMax,
        rootDispersionMax = with.rootDispersionMax,
        serverResponseDelayMax = with.serverResponseDelayMax,
        timeoutInMillis = with.connectionTimeoutInMillis,
        listener = listener,
    )

    private fun List<LongArray>.filterLeastRoundTripDelay(): LongArray {
        return minByOrNull { sntp.roundTripDelay(it) }
            ?: throw IllegalStateException("Could not find any results from requestingTime")
    }

    private fun List<LongArray>.filterMedianClockOffset(): LongArray {
        val sortedList = this.sortedBy { sntp.clockOffset(it) }
        return sortedList[sortedList.size / 2]
    }

    //endregion
}

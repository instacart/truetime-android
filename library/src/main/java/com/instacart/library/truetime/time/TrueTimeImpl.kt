package com.instacart.library.truetime.time

import com.instacart.library.truetime.log.Logger
import com.instacart.library.truetime.log.LoggerNoOp
import com.instacart.library.truetime.sntp.Sntp
import com.instacart.library.truetime.sntp.SntpImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Date

class TrueTimeImpl(
    private val logger: Logger = LoggerNoOp,
) : TrueTime2 {

    private val sntp: Sntp = SntpImpl(logger)
    private val timeKeeper = TimeKeeper(sntp)

    companion object {
        private const val TAG: String = "TrueTimeImpl"
    }

    override fun initialized(): Boolean = timeKeeper.hasTheTime()

    override fun initialize(with: TrueTimeParameters): Date {
        logger.v(TAG, "- initializing TrueTime")
        val ntpResult = init(with)
        logger.v(TAG, "- saving TrueTime NTP result")
        timeKeeper.save(ntpResult = ntpResult)
        logger.v(TAG, "- returning Time now")
        return timeKeeper.now()
    }

    override fun nowSafely(): Date {
        return if (timeKeeper.hasTheTime()) {
            logger.v(TAG, "TimeKeeper has the time")
            nowForced()
        } else {
            logger.d(TAG, "TimeKeeper does NOT have time: returning device time safely")
            Date()
        }
    }

    override fun nowForced(): Date {
        if (!initialized()) throw IllegalStateException("TrueTime was not initialized successfully yet")
        logger.v(TAG, "returning Time now")
        return timeKeeper.now()
    }

    override suspend fun sync(with: TrueTimeParameters): Job = withContext(Dispatchers.IO) {
        launch {
            while (true) {
                initialize(with)
                delay(with.syncIntervalInMillis)
                logger.v(TAG, "- starting next resync")
            }
        }
    }

    //region private helpers

    /**
     * Initialize TrueTime with an ntp pool server address
     */
    private fun init(with: TrueTimeParameters): LongArray {

        // resolve NTP pool -> single IPs
        return resolveNtpHostToIPs(with.ntpHostPool)
            // for each IP resolved
            .map { ipHost ->
                logger.v(TAG, "---- requesting time (single IP: $ipHost)")
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
    }

    /**
     * resolve ntp host pool address to single IPs
     */
    @Throws(UnknownHostException::class)
    private fun resolveNtpHostToIPs(ntpHost: String): List<String> {
        logger.v(TAG, "-- resolving ntpHost : $ntpHost")
        return InetAddress.getAllByName(ntpHost).map { it.hostAddress }
    }

    private fun requestTime(
        with: TrueTimeParameters,
        ipHostAddress: String,
    ): LongArray {
        // retrying upto (default 50) times if necessary
        repeat(with.retryCountAgainstSingleIp - 1) {
            try {
                // request Time
                logger.v(TAG, "------ requesting SNTP time")
                return sntpRequest(with, ipHostAddress)
            } catch (e: Exception) {
                logger.e(TAG, "------ Error requesting SNTP time", e)
            }
        }

        // last attempt
        logger.i(TAG, "---- last attempt for $ipHostAddress")
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
        with.connectionTimeoutInMillis
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

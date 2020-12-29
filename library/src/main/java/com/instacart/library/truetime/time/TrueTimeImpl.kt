package com.instacart.library.truetime.time

import android.os.SystemClock
import com.instacart.library.truetime.legacy.TrueLog
import com.instacart.library.truetime.sntp.Sntp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

class TrueTimeImpl(private val sntpClient: Sntp) : TrueTime2 {

    companion object {
        private const val TAG: String = "XXX-TrueTime"
    }

    private var ttResult: AtomicReference<TrueTimeResult> = AtomicReference()

    override fun initialized(): Boolean = ttResult.get() != null

    override suspend fun initialize(with: TrueTimeParameters): Date = withContext(Dispatchers.IO) {
        val ntpResult = init(with)
        val trueTimeResult = TrueTimeResult(ntpResult)
        ttResult.set(trueTimeResult)

        Date(trueTimeResult.timeSntp(sntpClient))
    }

    override fun nowSafely(): Date {
        return if (ttResult.get() != null) {
            nowForced()
        } else {
            TrueLog.d(TAG, "TrueTime not yet initialized")
            Date()
        }
    }

    override fun nowForced(): Date {
        if (!initialized()) throw IllegalStateException("TrueTime was not initialized successfully yet")
        return ttResult.get().timeNow(sntpClient)
    }

    /**
     * Initialize TrueTime with an ntp pool server address
     */
    private fun init(with: TrueTimeParameters): LongArray {

        // resolve NTP pool -> single IPs
        return resolveNtpHostToIPs(with.ntpHostPool)
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
    }

    /**
     * resolve ntp host pool address to single IPs
     */
    private fun resolveNtpHostToIPs(ntpHost: String): List<String> {
        TrueLog.d(TAG, "---- resolving ntpHost : $ntpHost")
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
                return sntpClient.requestTime(with, ipHostAddress)
            } catch (e: Exception) {
                TrueLog.e(TAG, "---- Error requesting time", e)
            }
        }

        // last attempt
        return sntpClient.requestTime(with, ipHostAddress)
    }

    private fun List<LongArray>.filterLeastRoundTripDelay(): LongArray {
        return minByOrNull { sntpClient.roundTripDelay(it) }
            ?: throw IllegalStateException("Could not find any results from requestingTime")
    }

    private fun List<LongArray>.filterMedianClockOffset(): LongArray {
        val sortedList = this.sortedBy { sntpClient.clockOffset(it) }
        return sortedList[sortedList.size / 2]
    }

    private data class TrueTimeResult(
        val ntpResult: LongArray
    ) {
        fun timeSntp(sntpClient: Sntp): Long = sntpClient.sntpTime(ntpResult)

        fun timeNow(sntpClient: Sntp): Date {
            val savedSntpTime: Long = timeSntp(sntpClient)
            val savedDeviceTime: Long = sntpClient.deviceTime(ntpResult)
            val currentDeviceTime: Long = SystemClock.elapsedRealtime()

            return Date(savedSntpTime + (currentDeviceTime - savedDeviceTime))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TrueTimeResult

            if (!ntpResult.contentEquals(other.ntpResult)) return false

            return true
        }

        override fun hashCode(): Int {
            return ntpResult.contentHashCode()
        }
    }
}

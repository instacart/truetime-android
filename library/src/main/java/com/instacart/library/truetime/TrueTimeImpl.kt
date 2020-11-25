package com.instacart.library.truetime

import com.instacart.library.truetime.sntp.Sntp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

class TrueTimeImpl(private val sntpClient: Sntp) : TrueTime2 {

    companion object {
        private const val TAG: String = "TrueTime"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // TODO: change this to a data class result value object
    private var initialized: AtomicBoolean = AtomicBoolean(false)

    override fun initialize(with: TrueTimeParameters) {
        TODO("Not yet implemented")
    }

    override fun now(): Date {
        TODO("Not yet implemented")
    }

    override fun nowSafely(): Date = if (initialized.get()) now() else Date()

    /**
     * Initialize TrueTime with an ntp pool server address
     *
     * @param ntpHost NTP pool server (time.apple.com, 0.us.pool.ntp.org, time.google.com)
     */
    private suspend fun initUsing(with: TrueTimeParameters): LongArray {

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
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun resolveNtpHostToIPs(ntpHost: String): List<String> = withContext(Dispatchers.IO) {
        TrueLog.d(TAG, "---- resolving ntpHost : $ntpHost")
        InetAddress.getAllByName(ntpHost).map { it.hostAddress }
    }

    private suspend fun requestTime(
        with: TrueTimeParameters,
        ipHostAddress: String,
    ): LongArray = withContext(Dispatchers.IO) {
        // retrying upto 50 times if necessary
         repeat(50 - 1) {
             try {
                 // request Time
                 return@withContext sntpClient.requestTime(with, ipHostAddress)
             } catch (e: Exception) {
                 TrueLog.e(TAG, "---- Error requesting time", e)
             }
        }

        // last attempt
        sntpClient.requestTime(with, ipHostAddress)
    }

    private fun List<LongArray>.filterLeastRoundTripDelay(): LongArray {
        return minByOrNull { sntpClient.getRoundTripDelay(it) }
            ?: throw IllegalStateException("Could not find any results from requestingTime")
    }

    private fun List<LongArray>.filterMedianClockOffset(): LongArray {
        val sortedList = this.sortedBy { sntpClient.getClockOffset(it) }
        return sortedList[sortedList.size / 2]
    }
}

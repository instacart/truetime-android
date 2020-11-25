package com.instacart.library.truetime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

class TrueTimeImpl : TrueTime2 {

    companion object {
        private const val TAG: String = "TrueTime"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
        // request Time
        // retrying upto 50 times if necessary
        TODO("Not yet implemented")
    }
}

private fun List<LongArray>.filterLeastRoundTripDelay(): LongArray {

    TODO("Not yet implemented")
}

private fun List<LongArray>.filterMedianClockOffset(): LongArray {
    val sortedList = this.sortedBy { SntpClient.getClockOffset(it) }
    return sortedList[sortedList.size / 2]
}

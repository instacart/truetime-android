package com.instacart.library.truetime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

        // for each IP resolved
            // 5 times against each IP
                // request Time
                    // retrying upto 50 times if necessary
            // collect 5 results to list
            // filter least round trip delay
            //  get single result for IP

        // collect max 5 of the IPs in a list
        // filter median clock offset
        //  get single long array

        TODO()
    }
}

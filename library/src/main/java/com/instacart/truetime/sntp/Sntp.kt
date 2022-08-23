package com.instacart.truetime.sntp

import java.io.IOException
import java.lang.Exception

interface Sntp {

    /**
     * See δ :
     * https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm
     */
    fun roundTripDelay(ntpResult: LongArray): Long

    /**
     * See θ :
     * https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm
     */
    fun clockOffset(ntpResult: LongArray): Long

    /**
     * @return NTP/"true" time when NTP call was made
     */
    fun trueTime(ntpResult: LongArray): Long

    /**
     * @return milliseconds since boot (including time spent in sleep) when NTP call was made
     */
    fun timeSinceBoot(ntpResult: LongArray): Long

    /**
     * Sends an NTP request to the given host and processes the response.
     *
     * @param ntpHostAddress    host name of the server.
     */
    @Throws(IOException::class)
    fun requestTime(
        ntpHostAddress: String,
        rootDelayMax: Float,
        rootDispersionMax: Float,
        serverResponseDelayMax: Int,
        timeoutInMillis: Int,
        listener: SntpEventListener,
    ): LongArray

}

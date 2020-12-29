package com.instacart.library.truetime.sntp

import com.instacart.library.truetime.time.TrueTimeParameters
import java.io.IOException

interface Sntp {

    /**
     * See δ :
     * https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm
     */
    fun roundTripDelay(ntpTimeResult: LongArray): Long

    /**
     * See θ :
     * https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm
     */
    fun clockOffset(ntpTimeResult: LongArray): Long

    /**
     * Time as derived from [ntpTimeResult].
     * This is basically "True Time" at the instant [ntpTimeResult] was received
     */
    fun sntpTime(ntpTimeResult: LongArray): Long

    fun deviceTime(ntpTimeResult: LongArray): Long

    /**
     * Sends an NTP request to the given host and processes the response.
     *
     * @param ntpHostAddress    host name of the server.
     */
    @Throws(IOException::class)
    fun requestTime(
        with: TrueTimeParameters,
        ntpHostAddress: String? = null,
    ): LongArray = requestTime(
        ntpHostAddress = ntpHostAddress ?: with.ntpHostPool,
        rootDelayMax = with.rootDelayMax,
        rootDispersionMax = with.rootDispersionMax,
        serverResponseDelayMax = with.serverResponseDelayMax,
        with.connectionTimeoutInMillis
    )

    @Throws(IOException::class)
    fun requestTime(
        ntpHostAddress: String,
        rootDelayMax: Float,
        rootDispersionMax: Float,
        serverResponseDelayMax: Int,
        timeoutInMillis: Int,
    ): LongArray

}
package com.instacart.library.truetime.sntp

interface Sntp {
    fun getRoundTripDelay(ntpTimeResult: LongArray): Long

    fun getClockOffset(ntpTimeResult: LongArray): Long
}
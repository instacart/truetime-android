package com.instacart.truetime.time

import android.os.SystemClock
import com.instacart.truetime.sntp.Sntp
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

// TODO: move android dependency to separate package

/**
 * Helper class that stores the NTP [LongArray] result
 * and derives true time from that result
 */
internal class TimeKeeper(
    private val sntp: Sntp
) {
    private var ttResult: AtomicReference<LongArray> = AtomicReference()

    fun hasTheTime(): Boolean = ttResult.get() != null

    fun save(ntpResult: LongArray) = ttResult.set(ntpResult)

    fun now(): Date {
        val ntpResult = ttResult.get()
        val savedSntpTime: Long = sntp.trueTime(ntpResult)
        val timeSinceBoot: Long = sntp.timeSinceBoot(ntpResult)
        val currentTimeSinceBoot: Long = SystemClock.elapsedRealtime()

        return Date(savedSntpTime + (currentTimeSinceBoot - timeSinceBoot))
    }
}

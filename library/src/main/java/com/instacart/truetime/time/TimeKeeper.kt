package com.instacart.truetime.time

import android.os.SystemClock
import com.instacart.truetime.TimeKeeperListener
import com.instacart.truetime.sntp.Sntp
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

// TODO: move android dependency to separate package

/**
 * Helper class that stores the NTP [LongArray] result
 * and derives true time from that result
 */
internal class TimeKeeper(
  private val sntp: Sntp,
  private val listener: TimeKeeperListener,
) {
    private var ttResult: AtomicReference<LongArray> = AtomicReference()

    fun hasTheTime(): Boolean = ttResult.get() != null

    fun save(ntpResult: LongArray) {
        listener.storingTrueTime(ntpResult)
        ttResult.set(ntpResult)
    }

    fun now(): Date {
        val ntpResult = ttResult.get()
        val savedSntpTime: Long = sntp.trueTime(ntpResult)
        val timeSinceBoot: Long = sntp.timeSinceBoot(ntpResult)
        val currentTimeSinceBoot: Long = SystemClock.elapsedRealtime()
        val trueTime = Date(savedSntpTime + (currentTimeSinceBoot - timeSinceBoot))

        listener.returningTrueTime(trueTime)
        return trueTime
    }
}

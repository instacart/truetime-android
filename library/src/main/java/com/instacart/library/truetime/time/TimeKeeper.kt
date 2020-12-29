package com.instacart.library.truetime.time

import android.os.SystemClock
import com.instacart.library.truetime.sntp.Sntp
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

// TODO: move android dependency to separate package

interface TimeKeeper {

    fun hasTheTime(): Boolean

    fun save(ntpResult: LongArray)

    fun now(): Date
}

class TimeKeeperImpl(
    private val sntp: Sntp
): TimeKeeper {
    private var ttResult: AtomicReference<LongArray> = AtomicReference()

    override fun hasTheTime(): Boolean = ttResult.get() != null

    override fun save(ntpResult: LongArray) = ttResult.set(ntpResult)

    override fun now(): Date {
        val ntpResult = ttResult.get()
        val savedSntpTime: Long = sntp.sntpTime(ntpResult)
        val savedDeviceTime: Long = sntp.deviceTime(ntpResult)
        val currentDeviceTime: Long = SystemClock.elapsedRealtime()

        return Date(savedSntpTime + (currentDeviceTime - savedDeviceTime))
    }
}

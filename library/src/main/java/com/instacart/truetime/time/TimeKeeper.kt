package com.instacart.truetime.time

import android.os.SystemClock
import com.instacart.truetime.CacheProvider
import com.instacart.truetime.TimeKeeperListener
import com.instacart.truetime.sntp.SntpResult
import java.util.*

// TODO: move android dependency to separate package
//  so we can make Truetime a pure kotlin library

/** TimeKeeper figures out how to give you the best time given all the info currently available. */
internal class TimeKeeper(
    private val listener: TimeKeeperListener,
    private val cacheProvider: CacheProvider,
) {

  fun save(ntpResult: SntpResult) {
    listener.storingTrueTime(ntpResult)
    cacheProvider.insert(ntpResult)
  }

  fun hasTheTime(): Boolean {
    if (!cacheProvider.hasAnyEntries()) return false

    val lastEntry = cacheProvider.fetchLatest()!!
    val currentElapsedTime = SystemClock.elapsedRealtime()
    val deviceRebooted = lastEntry.timeSinceBoot() > currentElapsedTime
    if (deviceRebooted) {
      cacheProvider.invalidate()
      listener.invalidateCacheOnRebootDetection()
    }

    return !deviceRebooted
  }

  fun nowSafely(): Date {
    return if (hasTheTime()) {
      nowTrueOnly(true)
    } else {
      listener.returningDeviceTime()
      Date()
    }
  }

  fun nowTrueOnly(hasTheTimeCalculated: Boolean = false): Date {
    if (hasTheTimeCalculated || !hasTheTime())
        throw IllegalStateException("TrueTime was not initialized successfully yet")
    return now()
  }

  /** Given the available information provide the best known time */
  private fun now(): Date {
    val ntpResult = cacheProvider.fetchLatest()!!
    val savedSntpTime: Long = ntpResult.trueTime()
    val timeSinceBoot: Long = ntpResult.timeSinceBoot()
    val currentTimeSinceBoot: Long = SystemClock.elapsedRealtime()
    val trueTime = Date(savedSntpTime + (currentTimeSinceBoot - timeSinceBoot))
    listener.returningTrueTime(trueTime)
    return trueTime
  }
}

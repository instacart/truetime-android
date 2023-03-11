package com.instacart.truetime.time

import android.os.SystemClock
import com.instacart.truetime.TimeKeeperListener
import com.instacart.truetime.sntp.SntpResult
import java.util.*
import java.util.concurrent.atomic.AtomicReference

// TODO: move android dependency to separate package
//  so we can make Truetime a pure kotlin library

/** TimeKeeper figures out how to give you the best time given all the info currently available. */
internal class TimeKeeper(
    private val listener: TimeKeeperListener,
) {
  private var ttResult: AtomicReference<SntpResult> = AtomicReference()

  /** stores the NTP [SntpResult] result and derives true time from that result */
  fun save(ntpResult: SntpResult) {
    listener.storingTrueTime(ntpResult)
    ttResult.set(ntpResult)
  }

  /** Is there sufficient information to determine the time */
  fun hasTheTime(): Boolean = ttResult.get() != null

  fun nowSafely(): Date {
    return if (hasTheTime()) {
      nowTrueOnly()
    } else {
      listener.returningDeviceTime()
      Date()
    }
  }

  fun nowTrueOnly(): Date {
    if (!hasTheTime()) throw IllegalStateException("TrueTime was not initialized successfully yet")
    return now()
  }

  /** Given the available information provide the best known time */
  private fun now(): Date {
    val ntpResult = ttResult.get()
    val savedSntpTime: Long = ntpResult.trueTime()
    val timeSinceBoot: Long = ntpResult.timeSinceBoot()
    val currentTimeSinceBoot: Long = SystemClock.elapsedRealtime()
    val trueTime = Date(savedSntpTime + (currentTimeSinceBoot - timeSinceBoot))

    listener.returningTrueTime(trueTime)
    return trueTime
  }
}

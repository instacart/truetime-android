package com.instacart.truetime.time

import android.os.SystemClock
import android.util.Log
import com.instacart.truetime.CacheProvider
import com.instacart.truetime.TimeKeeperListener
import com.instacart.truetime.sntp.SntpResult
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

// TODO: move android dependency to separate package
//  so we can make Truetime a pure kotlin library

/** TimeKeeper figures out how to give you the best time given all the info currently available. */
internal class TimeKeeper(
    private val listener: TimeKeeperListener,
    private val cacheProvider: CacheProvider,
) {

  private var atomicSlope: AtomicReference<Double> = AtomicReference(0.0)
  private var atomicIntercept: AtomicReference<Double> = AtomicReference(0.0)

  fun save(ntpResult: SntpResult) {
    listener.storingTrueTime(ntpResult)
    cacheProvider.insert(ntpResult)
    buildRegression()
  }

  fun hasTheTime(): Boolean = cacheProvider.hasInfo()

  fun nowSafely(syncInterval: Duration): Date {
    return if (hasTheTime()) {
      nowTrueOnly(syncInterval)
    } else {
      listener.returningDeviceTime()
      Date()
    }
  }

  fun nowTrueOnly(syncInterval: Duration): Date {
    if (!hasTheTime()) throw IllegalStateException("TrueTime was not initialized successfully yet")
    return now(syncInterval)
  }

  /** Given the available information provide the best known time */
  private fun now(syncInterval: Duration): Date {
    val lastNtpResult = cacheProvider.fetch()!!
    val currentTimeSinceBoot: Long = SystemClock.elapsedRealtime()

    val haveNotSyncedInAWhile =
        currentTimeSinceBoot - lastNtpResult.timeSinceBoot() > syncInterval.inWholeMilliseconds
    val atLeastTwoEntriesExist = cacheProvider.fetchAll().count() > 1

//    val trueTime: Date =
//        if (haveNotSyncedInAWhile || atLeastTwoEntriesExist) {
//          listener.usingRegressionEstimate()
//          Date(getCorrectedSavedTrueTime(currentTimeSinceBoot))
//        } else {
//          val savedTrueTime: Long = lastNtpResult.trueTime()
//          val savedTimeSinceBoot: Long = lastNtpResult.timeSinceBoot()
//          Date(savedTrueTime + (currentTimeSinceBoot - savedTimeSinceBoot))
//        }
    val trueTimeFromRegression: Date =
//        if (haveNotSyncedInAWhile || atLeastTwoEntriesExist) {
//          listener.usingRegressionEstimate()
          Date(getCorrectedSavedTrueTime(currentTimeSinceBoot))
//        } else {
          val savedTrueTime: Long = lastNtpResult.trueTime()
          val savedTimeSinceBoot: Long = lastNtpResult.timeSinceBoot()
          val trueTimeFromLastResult: Date = Date(savedTrueTime + (currentTimeSinceBoot - savedTimeSinceBoot))
//        }

    Log.d("TrueTime4", " ------- trueTimeFromRegression: $trueTimeFromRegression")
    Log.d("TrueTime4", " ------- trueTimeFromLastResult: $trueTimeFromLastResult")

    listener.returningTrueTime(trueTimeFromRegression)
    return trueTimeFromRegression
//    listener.returningTrueTime(trueTime)
//    return trueTime
  }

  private fun buildRegression() {
    // build the regression model
    //    y → savedTrueTime
    //    x → savedTimeSinceBoot
    val y = cacheProvider.fetchAll().map { it.trueTime().toDouble() }.toDoubleArray()
    val x = cacheProvider.fetchAll().map { it.timeSinceBoot().toDouble() }.toDoubleArray()

    // regression calculation
    val n = x.size
    val xSum = x.sum()
    val ySum = y.sum()
    val xxSum = x.sumOf { it * it }
    val xySum = x.zip(y).sumOf { (x, y) -> x * y }

    val slope = (n * xySum - xSum * ySum) / (n * xxSum - xSum * xSum)
    val intercept = (ySum - slope * xSum) / n

    atomicSlope.set(slope)
    atomicIntercept.set(intercept)
  }

  private fun getCorrectedSavedTrueTime(savedTimeSinceBoot: Long): Long {
    return (atomicSlope.get() * savedTimeSinceBoot + atomicIntercept.get()).toLong()
  }
}

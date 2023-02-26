package com.instacart.truetime.time

import java.util.Date
import kotlinx.coroutines.Job

interface TrueTime {

  /**
   * Run [com.instacart.truetime.sntp.Sntp.requestTime] in the background repeatedly to account for
   * clock drift and update the locally stored SNTP result
   *
   * @return Use this Coroutines job to cancel the [sync] and all background work
   */
  fun sync(): Job

  fun hasTheTime(): Boolean

  /**
   * This is [TrueTime]'s main function to get time It should respect
   * [TrueTimeParameters.returnSafelyWhenUninitialized] setting
   */
  fun now(): Date

  /**
   * return the current time as calculated by TrueTime. If TrueTime doesn't [hasTheTime], will throw
   * [IllegalStateException]
   */
  @Throws(IllegalStateException::class) fun nowTrueOnly(): Date

  /** return [nowTrueOnly] if TrueTime is available otherwise fallback to System clock date */
  fun nowSafely(): Date = if (hasTheTime()) nowTrueOnly() else Date()
}

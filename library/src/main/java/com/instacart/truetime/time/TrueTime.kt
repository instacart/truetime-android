package com.instacart.truetime.time

import kotlinx.coroutines.Job
import java.time.Instant
import java.util.*

/** This is the main class that has the APIs for accessing Truetime. */
interface TrueTime {

  /**
   * Run [com.instacart.truetime.sntp.Sntp.requestTime] in the background repeatedly to account for
   * clock drift and update the locally stored SNTP result
   *
   * @return Use this Coroutines job to cancel the [sync] and all background work
   */
  fun sync(): Job

  /**
   * This is [TrueTime]'s main function to get time It should respect
   * [TrueTimeParameters.returnSafelyWhenUninitialized] setting
   */
  fun now(): Instant

  /**
   * return the current time as calculated by TrueTime. If TrueTime doesn't [hasTheTime], will throw
   * [IllegalStateException]
   */
  @Throws(IllegalStateException::class) fun nowTrueOnly(): Instant

  /** return [nowTrueOnly] if TrueTime is available otherwise fallback to System clock date */
  fun nowSafely(): Instant

  /** Does [TrueTime] have the "true" time or about to default to device time */
  fun hasTheTime(): Boolean
}

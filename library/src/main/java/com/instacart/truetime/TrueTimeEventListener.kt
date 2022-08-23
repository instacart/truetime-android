package com.instacart.truetime

import com.instacart.truetime.time.TrueTimeParameters
import java.util.*

interface TrueTimeEventListener : SntpEventListener, TimeKeeperListener {

    /**
     * TrueTime initialize call performed
     */
    fun initialize(with: TrueTimeParameters)

    /**
     * TrueTime came back with [ntpResult]
     */
    fun initializeSuccess(ntpResult: LongArray)

    /**
     * Main TrueTime initialization call failed
     * with a generic exception [e]
     */
    fun initializeFailed(e: Exception)

    /**
     * The next TrueTime initialization call
     * will be attempted in [delay]
     */
    fun nextInitializeIn(delay: Long)

    /**
     * Resolved NTP pool host address [ntpHost]
     * to the list of IP addresses [ipList]
     */
    fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<String>)

    fun sntpRequestLastAttempt(ipHost: String)

    /**
     * SNTP request failed with exception [e]
     */
    fun sntpRequestFailed(e: Exception)
}

interface SntpEventListener {

    /**
     * requesting time from [ntpHost] which should be an IP address
     */
    fun requestingTime(ntpHost: String)

    /**
     * Invoked after the SNTP request to [ntpHost] comes back successful
     */
    fun responseSuccessful(ntpHost: String)

    /**
     * Invoked if the SNTP request to [ntpHost] fails for any reason
     */
    fun responseFailed(ntpHost: String, e: Exception)
}

interface TimeKeeperListener {
  fun storingTrueTime(ntpResult: LongArray)

  /**
   * TimeKeeper has the "true" time
   * and is returning it
   */
  fun returningTrueTime(trueTime: Date)

  /**
   * TimeKeeper does not have the "true" time
   * returning device time.
   */
  fun returningDeviceTime()
}

object NoOpEventListener : TrueTimeEventListener {
  override fun initialize(with: TrueTimeParameters) {}
  override fun initializeSuccess(ntpResult: LongArray) {}
  override fun initializeFailed(e: Exception) {}
  override fun nextInitializeIn(delay: Long) {}
  override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<String>) {}
  override fun sntpRequestLastAttempt(ipHost: String) {}
  override fun sntpRequestFailed(e: Exception) {}

  override fun requestingTime(ntpHost: String) {}
  override fun responseSuccessful(ntpHost: String) {}
  override fun responseFailed(ntpHost: String, e: Exception) {}

  override fun storingTrueTime(ntpResult: LongArray) {}
  override fun returningTrueTime(trueTime: Date) {}
  override fun returningDeviceTime() {}
}

package com.instacart.truetime

import com.instacart.truetime.sntp.SntpResult
import com.instacart.truetime.time.TrueTimeParameters
import java.net.InetAddress
import java.util.*

interface TrueTimeEventListener : SntpEventListener, TimeKeeperListener {
  /** [com.instacart.truetime.time.TrueTime] initialize call performed */
  fun initialize(params: TrueTimeParameters)

  fun initializeSuccess(ntpResult: SntpResult)

  /** initialization call failed with a generic exception [e] */
  fun initializeFailed(e: Exception)

  /** next initialization call will be made in [delayInMillis] */
  fun nextInitializeIn(delayInMillis: Long)

  /** resolved NTP pool host address [ntpHost] to the list of IP addresses [ipList] */
  fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<InetAddress>)

  fun lastSntpRequestAttempt(ipHost: InetAddress)

  fun sntpRequestFailed(e: Exception)

  /** CoroutineDispatcher exception from dispatcher that's responsible for syncing TrueTime */
  fun syncDispatcherException(t: Throwable)
}

interface SntpEventListener {
  /** requesting time from [address] which should be an IP address */
  fun sntpRequest(address: InetAddress)

  fun sntpRequestSuccessful(address: InetAddress)

  /** Invoked if the SNTP request to [address] fails for any reason */
  fun sntpRequestFailed(address: InetAddress, e: Exception)
}

interface TimeKeeperListener {
  fun storingTrueTime(ntpResult: LongArray)

  /** TimeKeeper has the "true" time and is returning it */
  fun returningTrueTime(trueTime: Date)

  /** TimeKeeper does not have the "true" time returning device time. */
  fun returningDeviceTime()
}

object NoOpEventListener : TrueTimeEventListener {
  override fun initialize(params: TrueTimeParameters) {}
  override fun initializeSuccess(ntpResult: SntpResult) {}
  override fun initializeFailed(e: Exception) {}
  override fun nextInitializeIn(delayInMillis: Long) {}
  override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<InetAddress>) {}
  override fun lastSntpRequestAttempt(ipHost: InetAddress) {}
  override fun sntpRequestFailed(e: Exception) {}
  override fun syncDispatcherException(t: Throwable) {}

  override fun sntpRequest(address: InetAddress) {}
  override fun sntpRequestSuccessful(address: InetAddress) {}
  override fun sntpRequestFailed(address: InetAddress, e: Exception) {}

  override fun storingTrueTime(ntpResult: LongArray) {}
  override fun returningTrueTime(trueTime: Date) {}
  override fun returningDeviceTime() {}
}

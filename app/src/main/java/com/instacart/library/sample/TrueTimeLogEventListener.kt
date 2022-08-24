package com.instacart.library.sample

import android.util.Log
import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.time.TrueTimeParameters
import java.util.Date

class TrueTimeLogEventListener: TrueTimeEventListener {
  override fun initialize(params: TrueTimeParameters) {
    Log.v("TrueTime", "initialize call performed with $params")
  }

  override fun initializeSuccess(ntpResult: LongArray) {
    Log.i("TrueTime", "came back successfully with $ntpResult")
  }

  override fun initializeFailed(e: Exception) {
    Log.e("TrueTime", "initialization call failed with a generic exception", e)
  }

  override fun nextInitializeIn(delayInMillis: Long) {
    Log.v("TrueTime", " next initialization call will be made in $delayInMillis ms")
  }

  override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<String>) {
    Log.v("TrueTime", "resolved NTP pool host address $ntpHost to the list of IP addresses $ipList")
  }

  override fun lastSntpRequestAttempt(ipHost: String) {
    Log.v("TrueTime", "This is the last SNTP request attempt to $ipHost")
  }

  override fun sntpRequestFailed(e: Exception) {
    Log.e("TrueTime", "SNTP request failed", e)
  }

  override fun syncDispatcherException(t: Throwable) {
    Log.e("TrueTime", "CoroutineDispatcher exception from TrueTime sync call", t)
  }

  override fun sntpRequest(ntpHost: String) {
    Log.v("TrueTime SNTP", "SNTP request to $ntpHost")
  }

  override fun sntpRequestSuccessful(ntpHost: String) {
    Log.v("TrueTime SNTP", "SNTP Request to $ntpHost came back successfully")
  }

  override fun sntpRequestFailed(ntpHost: String, e: Exception) {
    Log.e("TrueTime SNTP", "SNTP Request to $ntpHost failed", e)
  }

  override fun storingTrueTime(ntpResult: LongArray) {
    Log.v("TrueTime TimeKeeper", "TimeKeeper storing time $ntpResult")
  }

  override fun returningTrueTime(trueTime: Date) {
    Log.v("TrueTime TimeKeeper", "returning TrueTime $trueTime" )
  }

  override fun returningDeviceTime() {
    Log.v("TrueTime TimeKeeper", "returning Device Time" )
  }
}

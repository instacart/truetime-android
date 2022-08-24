package com.instacart.library.sample

import android.util.Log
import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.time.TrueTimeParameters
import java.util.Date

class TrueTimeLogEventListener: TrueTimeEventListener {
  override fun initialize(params: TrueTimeParameters) {
    Log.v("TrueTime4", "initialize call performed with $params")
  }

  override fun initializeSuccess(ntpResult: LongArray) {
    Log.i("TrueTime4", "came back successfully with $ntpResult")
  }

  override fun initializeFailed(e: Exception) {
    Log.e("TrueTime4", "initialization call failed with a generic exception", e)
  }

  override fun nextInitializeIn(delayInMillis: Long) {
    Log.v("TrueTime4", " next initialization call will be made in $delayInMillis ms")
  }

  override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<String?>) {
    Log.v("TrueTime4", "resolved NTP pool host address $ntpHost to the list of IP addresses $ipList")
  }

  override fun lastSntpRequestAttempt(ipHost: String) {
    Log.v("TrueTime4", "This is the last SNTP request attempt to $ipHost")
  }

  override fun sntpRequestFailed(e: Exception) {
    Log.e("TrueTime4", "SNTP request failed", e)
  }

  override fun syncDispatcherException(t: Throwable) {
    Log.e("TrueTime4", "CoroutineDispatcher exception from TrueTime sync call", t)
  }

  override fun sntpRequest(ntpHost: String) {
    Log.v("TrueTime4 SNTP", "SNTP request to $ntpHost")
  }

  override fun sntpRequestSuccessful(ntpHost: String) {
    Log.v("TrueTime4 SNTP", "SNTP Request to $ntpHost came back successfully")
  }

  override fun sntpRequestFailed(ntpHost: String, e: Exception) {
//    Log.e("TrueTime4 SNTP", "SNTP Request to $ntpHost failed", e)
  }

  override fun storingTrueTime(ntpResult: LongArray) {
    Log.v("TrueTime4 TimeKeeper", "TimeKeeper storing time $ntpResult")
  }

  override fun returningTrueTime(trueTime: Date) {
    Log.v("TrueTime4 TimeKeeper", "returning TrueTime $trueTime" )
  }

  override fun returningDeviceTime() {
    Log.v("TrueTime4 TimeKeeper", "returning Device Time" )
  }
}

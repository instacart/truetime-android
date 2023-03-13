package com.instacart.sample

import android.util.Log
import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.sntp.SntpResult
import com.instacart.truetime.time.TrueTimeParameters
import java.net.InetAddress
import java.util.*

class TrueTimeLogEventListener : TrueTimeEventListener {
  override fun initialize(params: TrueTimeParameters) {
    Log.v("TrueTime4", "initialize call performed with $params")
  }

  override fun initializeSuccess(ntpResult: SntpResult) {
    Log.i("TrueTime4", "came back successfully with $ntpResult")
  }

  override fun initializeFailed(e: Exception) {
    Log.e("TrueTime4", "initialization call failed with a generic exception", e)
  }

  override fun nextInitializeIn(delayInMillis: Long) {
    Log.v("TrueTime4", " next initialization call will be made in $delayInMillis ms")
  }

  override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<InetAddress>) {
    Log.v(
        "TrueTime4", "resolved NTP pool host address $ntpHost to the list of IP addresses $ipList")
  }

  override fun lastSntpRequestAttempt(ipHost: InetAddress) {
    Log.v("TrueTime4", "This is the last SNTP request attempt to $ipHost")
  }

  override fun sntpRequestFailed(e: Exception) {
    Log.e("TrueTime4", "SNTP request failed", e)
  }

  override fun syncDispatcherException(t: Throwable) {
    Log.e("TrueTime4", "CoroutineDispatcher exception from TrueTime sync call", t)
  }

  override fun sntpRequest(address: InetAddress) {
    Log.v("TrueTime4 SNTP", "SNTP request to $address")
  }

  override fun sntpRequestSuccessful(address: InetAddress) {
    Log.v("TrueTime4 SNTP", "SNTP Request to $address came back successfully")
  }

  override fun sntpRequestFailed(address: InetAddress, e: Exception) {
    //    Log.e("TrueTime4 SNTP", "SNTP Request to $ntpHost failed", e)
  }

  override fun storingTrueTime(ntpResult: SntpResult) {
    Log.v("TrueTime4 TimeKeeper", "TimeKeeper storing time $ntpResult")
  }

  override fun returningTrueTime(trueTime: Date) {
    Log.v("TrueTime4 TimeKeeper", "returning TrueTime $trueTime")
  }

  override fun returningDeviceTime() {
    Log.v("TrueTime4 TimeKeeper", "returning Device Time")
  }
}

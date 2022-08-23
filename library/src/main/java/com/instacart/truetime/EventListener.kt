package com.instacart.truetime

import com.instacart.truetime.sntp.SntpEventListener
import java.lang.Exception

interface EventListener: SntpEventListener {
    fun trueTimeInitialized(msg: String = "TrueTime ")
}

object NoOpEventListener: EventListener {
  override fun trueTimeInitialized(msg: String) {}
  override fun responseSuccessful(ntpHost: String) {}
  override fun responseFailed(ntpHost: String, e: Exception) {}
}

package com.instacart.truetime.sntp

import java.lang.Exception

interface SntpEventListener {

  /**
   * Invoked after the SNTP request to [ntpHost] comes back successful
   */
  fun responseSuccessful(ntpHost:String)

  /**
   * Invoked if the SNTP request to [ntpHost] fails for any reason
   */
  fun responseFailed(ntpHost: String, e: Exception)
}

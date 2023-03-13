package com.instacart.truetime.sntp

import com.instacart.truetime.SntpEventListener
import java.io.IOException
import java.net.InetAddress

interface Sntp {
  /**
   * Sends an NTP request to the given host and processes the response.
   *
   * @param ntpHostAddress host name of the server.
   */
  @Throws(IOException::class)
  fun requestTime(
      ntpHostAddress: InetAddress,
      rootDelayMax: Float,
      rootDispersionMax: Float,
      serverResponseDelayMax: Int,
      timeoutInMillis: Int,
      listener: SntpEventListener,
  ): SntpResult
}

package com.instacart.truetime.sntp

class SntpResult(val ntpResult: LongArray) {

  /** See δ : https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm */
  fun roundTripDelay(): Long {
    return ntpResult[SntpImpl.RESPONSE_INDEX_RESPONSE_TIME] -
        ntpResult[SntpImpl.RESPONSE_INDEX_ORIGINATE_TIME] -
        (ntpResult[SntpImpl.RESPONSE_INDEX_TRANSMIT_TIME] -
            ntpResult[SntpImpl.RESPONSE_INDEX_RECEIVE_TIME])
  }

  /** See θ : https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm */
  fun clockOffset(): Long {
    return (ntpResult[SntpImpl.RESPONSE_INDEX_RECEIVE_TIME] -
        ntpResult[SntpImpl.RESPONSE_INDEX_ORIGINATE_TIME] +
        (ntpResult[SntpImpl.RESPONSE_INDEX_TRANSMIT_TIME] -
            ntpResult[SntpImpl.RESPONSE_INDEX_RESPONSE_TIME])) / 2
  }

  /** @return "true" time when NTP call was made */
  fun trueTime(): Long {
    return responseTime() + clockOffset()
  }

  /** @return milliseconds since boot (including time spent in sleep) when NTP call was made */
  fun timeSinceBoot(): Long {
    return ntpResult[SntpImpl.RESPONSE_INDEX_RESPONSE_TICKS]
  }

  private fun responseTime(): Long {
    return ntpResult[SntpImpl.RESPONSE_INDEX_RESPONSE_TIME]
  }
}

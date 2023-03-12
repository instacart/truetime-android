package com.instacart.truetime

import com.instacart.truetime.sntp.SntpResult

interface CacheProvider {
  fun insert(result: SntpResult)
  fun fetch(): SntpResult?
  fun fetchAll(): Iterable<SntpResult>
  /** Is there sufficient information to determine the time */
  fun hasInfo(): Boolean
}

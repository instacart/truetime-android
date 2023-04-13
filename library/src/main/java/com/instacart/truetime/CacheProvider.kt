package com.instacart.truetime

import com.instacart.truetime.sntp.SntpResult

interface CacheProvider {
  fun insert(result: SntpResult)
  fun fetchLatest(): SntpResult?
  fun fetchAll(): Iterable<SntpResult>
  fun hasAnyEntries(): Boolean
  fun invalidate()
}

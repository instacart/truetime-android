package com.instacart.truetime

import com.instacart.truetime.sntp.SntpResult
import java.util.concurrent.LinkedBlockingDeque

class BasicCacheProvider : CacheProvider {
  private val stack = LinkedBlockingDeque<SntpResult>(3)

  override fun insert(result: SntpResult) {
    if (!stack.offerFirst(result)) {
      stack.removeLast()
      stack.addFirst(result)
    }
  }

  override fun fetchLatest(): SntpResult? = stack.peekFirst()

  override fun fetchAll(): Iterable<SntpResult> = stack.asIterable()

  override fun hasAnyEntries(): Boolean = stack.size > 0

  override fun invalidate() = stack.clear()
}

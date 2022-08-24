package com.instacart.truetime.time

class TrueTimeParameters private constructor(
  val connectionTimeoutInMillis: Int,
  val ntpHostPool: ArrayList<String>,
  val retryCountAgainstSingleIp: Int,
  val rootDelayMax: Float,
  val rootDispersionMax: Float,
  val serverResponseDelayMax: Int,
  val syncIntervalInMillis: Long,
  val shouldReturnSafely: Boolean,
) {

  class Builder {

    private var connectionTimeoutInMillis: Int = 30_000

    fun connectionTimeoutInMillis(value: Int): Builder {
      connectionTimeoutInMillis = value
      return this
    }

    private var ntpHostPool: ArrayList<String> = arrayListOf("time.google.com")

    // TODO: currently only using the first ntp host
    //  in the future we want to leverage all the time providers
    fun ntpHostPool(value: ArrayList<String>): Builder {
      ntpHostPool = value
      return this
    }

    private var retryCountAgainstSingleIp: Int = 50

    fun retryCountAgainstSingleIp(value: Int): Builder {
      retryCountAgainstSingleIp = value
      return this
    }

    private var rootDelayMax: Float = 100f

    fun rootDelayMax(value: Float): Builder {
      rootDelayMax = value
      return this
    }

    private var rootDispersionMax: Float = 100f

    fun rootDispersionMax(value: Float): Builder {
      rootDispersionMax = value
      return this
    }

    private var serverResponseDelayMax: Int = 750

    fun serverResponseDelayMax(value: Int): Builder {
      serverResponseDelayMax = value
      return this
    }

    // re-sync every 1 hour by default
    private var syncIntervalInMillis: Long = 3600_000

    fun syncIntervalInMillis(value: Long): Builder {
      syncIntervalInMillis = value
      return this
    }

    /**
     * if set to [true] it will default to [TrueTime.nowSafely]
     * else will return [TrueTime.nowTrueOnly]
     */
    private var shouldReturnSafely: Boolean = true

    fun shouldReturnSafely(value: Boolean): Builder {
      shouldReturnSafely = value
      return this
    }

    // TODO: cache provider
    //  val cacheProvider: TrueTimeCacheProvider? = null,

    fun buildParams() = TrueTimeParameters(
      connectionTimeoutInMillis,
      ntpHostPool,
      retryCountAgainstSingleIp,
      rootDelayMax,
      rootDispersionMax,
      serverResponseDelayMax,
      syncIntervalInMillis,
      shouldReturnSafely
    )
  }
}

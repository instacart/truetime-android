package com.instacart.truetime.time

class TrueTimeParameters private constructor(
  val connectionTimeoutInMillis: Int,
  val ntpHostPool: ArrayList<String>,
  val retryCountAgainstSingleIp: Int,
  val rootDelayMax: Float,
  val rootDispersionMax: Float,
  val serverResponseDelayMax: Int,
  val syncIntervalInMillis: Long,
) {

  class Builder {

    private var connectionTimeoutInMillis: Int = 30_000

    fun connectionTimeoutInMillis(value: Int): Builder {
      connectionTimeoutInMillis = value
      return this
    }

    private var ntpHostPool: ArrayList<String> = arrayListOf("time.google.com")

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

//    TODO: cache provider
//    val cacheProvider: TrueTimeCacheProvider? = null,

//    TODO: currently only using the first ntp host
//    in the future we want to leverage all the time providers

    fun buildParams() = TrueTimeParameters(
      connectionTimeoutInMillis,
      ntpHostPool,
      retryCountAgainstSingleIp,
      rootDelayMax,
      rootDispersionMax,
      serverResponseDelayMax,
      syncIntervalInMillis,
    )
  }
}

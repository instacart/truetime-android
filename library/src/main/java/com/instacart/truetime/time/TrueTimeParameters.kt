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
  val filterIPV6Addresses: Boolean,
  val strictMode: Boolean,
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

    fun safeReturnMode(value: Boolean): Builder {
      shouldReturnSafely = value
      return this
    }

    /**
     * Certain NTP hosts like time.google.com return IPV6 addresses.
     * This can cause problems (atleast in emulators) so filter by default.
     *
     * In practice, on real devices [InetAddress.getAllByName] tends to return
     * only IPV4 addresses.
     */
    private var filterIPV6Addresses: Boolean = true

    fun filterIPV6Addresses(value: Boolean): Builder {
      filterIPV6Addresses = value
      return this
    }

    /**
     *  The NTP spec requires a pretty strict set of SNTP call sequence to be made
     *    we resolve the ntp pool to single IPs (typically around 4-5 IP addresses)
     *    we now make 5 calls to each of these IPs (~ 5 * 4-5 calls)
     *    and if a call fails we repeat it at least [retryCountAgainstSingleIp] times.
     *
     *  if [strictCalculationMode] is true, we ignore all of the above and return as soon as we get
     *  at least one successful SNTP call. This is what many other common libraries do.
     */
    private var strictMode: Boolean = true

    fun strictCalculationMode(value: Boolean): Builder {
      strictMode = value
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
      shouldReturnSafely,
      filterIPV6Addresses,
      strictMode,
    )
  }
}

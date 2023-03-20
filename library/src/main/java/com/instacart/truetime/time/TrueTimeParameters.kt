package com.instacart.truetime.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class TrueTimeParameters
private constructor(
  val connectionTimeout: Duration,
  val ntpHostPool: ArrayList<String>,
  val retryCountAgainstSingleIp: Int,
  val rootDelayMax: Float,
  val rootDispersionMax: Float,
  val serverResponseDelayMax: Duration,
  val syncInterval: Duration,
  val returnSafelyWhenUninitialized: Boolean,
  val filterIpv6Addresses: Boolean,
  val strictNtpMode: Boolean,
) {

  class Builder {

    private var connectionTimeout: Duration = 30_000.milliseconds

    fun connectionTimeout(value: Duration): Builder {
      connectionTimeout = value
      return this
    }

    private var ntpHostPool: ArrayList<String> = arrayListOf("time.google.com")

    // TODO: Utilize all ntp pool addresses from `TrueTimeParameters.ntpHostPool`
    //  currently only using the first ntp host
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

    private var serverResponseDelayMax: Duration = 750.milliseconds

    fun serverResponseDelayMax(value: Duration): Builder {
      serverResponseDelayMax = value
      return this
    }

    // re-sync every 1 hour by default
    private var syncInterval: Duration = 1.hours

    fun syncInterval(value: Duration): Builder {
      syncInterval = value
      return this
    }

    /**
     * if set to [true] it will default to [TrueTime.nowSafely] else will return
     * [TrueTime.nowTrueOnly]
     */
    private var returnSafelyWhenUninitialized: Boolean = true

    fun returnSafelyWhenUninitialized(value: Boolean): Builder {
      returnSafelyWhenUninitialized = value
      return this
    }

    /**
     * Certain NTP hosts like time.google.com return IPV6 addresses. This can cause problems
     * (atleast in emulators) so filter by default.
     *
     * In practice, on real devices [InetAddress.getAllByName] tends to return only IPV4 addresses.
     */
    private var filterIpv6Addresses: Boolean = true

    fun filterIpv6Addresses(value: Boolean): Builder {
      filterIpv6Addresses = value
      return this
    }

    /**
     * The NTP spec requires a pretty strict set of SNTP call sequence to be made we resolve the ntp
     * pool to single IPs (typically around 4-5 IP addresses) we now make 5 calls to each of these
     * IPs (~ 5 * 4-5 calls) and if a call fails we repeat it at least [retryCountAgainstSingleIp]
     * times.
     *
     * if [strictNtpMode] is false, we ignore all of the above and return as soon as we get at least
     * one successful SNTP call. This is what many other common libraries do.
     */
    private var strictNtpMode: Boolean = true

    fun strictNtpMode(value: Boolean): Builder {
      strictNtpMode = value
      return this
    }

    // TODO: Introduce a Cache provider
    //  val cacheProvider: TrueTimeCacheProvider? = null,

    fun buildParams() =
        TrueTimeParameters(
            connectionTimeout,
            ntpHostPool,
            retryCountAgainstSingleIp,
            rootDelayMax,
            rootDispersionMax,
            serverResponseDelayMax,
            syncInterval,
            returnSafelyWhenUninitialized,
            filterIpv6Addresses,
            strictNtpMode,
        )
  }
}

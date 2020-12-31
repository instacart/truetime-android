package com.instacart.library.truetime.time

import com.instacart.library.truetime.cache.TrueTimeCacheProvider

data class TrueTimeParameters(

    // TODO: cache provider
    val cacheProvider: TrueTimeCacheProvider? = null,

    val connectionTimeoutInMillis: Int = 30_000,
    val ntpHostPool: String = "time.google.com",
    val retryCountAgainstSingleIp: Int = 50,
    val rootDelayMax: Float = 100f,
    val rootDispersionMax: Float = 100f,
    val serverResponseDelayMax: Int = 750,
    val syncIntervalInMillis: Long = 3600_000, // re-sync every 1 hour by default
)
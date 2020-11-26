package com.instacart.library.truetime.time

import com.instacart.library.truetime.cache.TrueTimeCacheProvider

data class TrueTimeParameters(
    val showLogs: Boolean = false,
    val ntpHostPool: String = "time.google.com",
    val rootDelayMax: Float = 100f,
    val rootDispersionMax: Float = 100f,
    val serverResponseDelayMax: Int = 750,
    val connectionTimeoutInMillis: Int = 30_000,
    val cacheProvider: TrueTimeCacheProvider? = null
)
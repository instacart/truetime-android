package com.instacart.library.truetime

import java.util.Date

interface TrueTime2 {

    fun initialize(with: TrueTimeParameters = TrueTimeParameters())

    fun now(): Date

    /**
     * return [now] if TrueTime is available otherwise
     * fallback to System clock date
     */
    fun nowSafely(): Date
}

data class TrueTimeParameters(
    val showLogs: Boolean = false,
    val ntpHost: String = "time.google.com",
    val connectionTimeoutInMillis: Long = 30_000,
    val cacheProvider: TrueTimeCacheProvider? = null
)

interface TrueTimeCacheProvider {
    fun clear()

    fun update(
        bootTime: Long? = null,
        deviceUptime: Long? = null,
        sntpTime: Long? = null
    )
}
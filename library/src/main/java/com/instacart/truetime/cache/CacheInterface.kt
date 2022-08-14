package com.instacart.truetime.cache

interface CacheInterface {

    companion object {
        const val KEY_CACHED_BOOT_TIME = "com.instacart.truetime.cached_boot_time"
        const val KEY_CACHED_DEVICE_UPTIME = "com.instacart.truetime.cached_device_uptime"
        const val KEY_CACHED_SNTP_TIME = "com.instacart.truetime.cached_sntp_time"
    }

    fun put(key: String, value: Long)

    fun get(key: String, defaultValue: Long): Long

    fun clear()
}

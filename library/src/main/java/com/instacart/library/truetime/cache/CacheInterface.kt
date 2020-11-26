package com.instacart.library.truetime.cache

interface CacheInterface {

    companion object {
        const val KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time"
        const val KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime"
        const val KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time"
    }

    fun put(key: String, value: Long)

    fun get(key: String, defaultValue: Long): Long

    fun clear()
}

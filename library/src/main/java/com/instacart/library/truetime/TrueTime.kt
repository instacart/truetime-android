package com.instacart.library.truetime

import android.content.Context
import android.os.SystemClock
import java.io.IOException
import java.util.Date
import java.util.Locale

open class TrueTime {

    companion object {

        private val TAG = TrueTime::class.java.simpleName

        private val INSTANCE = TrueTime()
        private val DISK_CACHE_CLIENT = DiskCacheClient()
        private val SNTP_CLIENT = SntpClient()

        private var rootDelayMax = 100f
        private var rootDispersionMax = 100f
        private var serverResponseDelayMax = 750
        private var udpSocketTimeoutInMillis = 30000

        /**
         * @return Date object that returns the current time in the default Timezone
         */
        fun now(): Date {
            if (!isInitialized) {
                throw IllegalStateException("You need to call init() on TrueTime at least once.")
            }

            val cachedSntpTime = getCachedSntpTime()
            val cachedDeviceUptime = getCachedDeviceUptime()
            val deviceUptime = SystemClock.elapsedRealtime()
            val now = cachedSntpTime + (deviceUptime - cachedDeviceUptime)

            return Date(now)
        }

        val isInitialized: Boolean
            get() = SNTP_CLIENT.wasInitialized() || DISK_CACHE_CLIENT.isTrueTimeCachedFromAPreviousBoot()

        fun build(): TrueTime {
            return INSTANCE
        }

        /**
         * clear the cached TrueTime info on device reboot.
         */
        fun clearCachedInfo() {
            DISK_CACHE_CLIENT.clearCachedInfo()
        }

        @Synchronized fun saveTrueTimeInfoToDisk() {
            if (!SNTP_CLIENT.wasInitialized()) {
                TrueLog.i(TAG, "---- SNTP client not available. not caching TrueTime info in disk")
                return
            }
            DISK_CACHE_CLIENT.cacheTrueTimeInfo(SNTP_CLIENT)
        }

        private fun getCachedDeviceUptime(): Long {
            val cachedDeviceUptime = if (SNTP_CLIENT.wasInitialized())
                SNTP_CLIENT.cachedDeviceUptime
            else
                DISK_CACHE_CLIENT.getCachedDeviceUptime()

            if (cachedDeviceUptime == 0L) {
                throw RuntimeException("expected device time from last boot to be cached. couldn't find it.")
            }

            return cachedDeviceUptime
        }

        private fun getCachedSntpTime(): Long {
            val cachedSntpTime = if (SNTP_CLIENT.wasInitialized())
                SNTP_CLIENT.cachedSntpTime
            else
                DISK_CACHE_CLIENT.getCachedSntpTime()

            if (cachedSntpTime == 0L) {
                throw RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.")
            }

            return cachedSntpTime
        }
    }

    private var _ntpHost = "1.us.pool.ntp.org"

    @Throws(IOException::class)
    fun initialize() {
        initialize(_ntpHost)
    }

    /**
     * Cache TrueTime initialization information in SharedPreferences
     * This can help avoid additional TrueTime initialization on app kills
     */
    @Synchronized open fun withSharedPreferencesCache(context: Context): TrueTime {
        DISK_CACHE_CLIENT.enableCacheInterface(SharedPreferenceCacheImpl(context))
        return INSTANCE
    }

    /**
     * Customized TrueTime Cache implementation.
     */
    @Synchronized open fun withCustomizedCache(cacheInterface: CacheInterface): TrueTime {
        DISK_CACHE_CLIENT.enableCacheInterface(cacheInterface)
        return INSTANCE
    }

    @Synchronized open fun withConnectionTimeout(timeoutInMillis: Int): TrueTime {
        udpSocketTimeoutInMillis = timeoutInMillis
        return INSTANCE
    }

    @Synchronized open fun withRootDelayMax(rootDelayMax: Float): TrueTime {
        if (rootDelayMax > rootDelayMax) {
            val log = String.format(
                Locale.getDefault(),
                "The recommended max rootDelay value is %f. You are setting it at %f",
                rootDelayMax, rootDelayMax
            )
            TrueLog.w(TAG, log)
        }

        TrueTime.rootDelayMax = rootDelayMax
        return INSTANCE
    }

    @Synchronized open fun withRootDispersionMax(rootDispersionMax: Float): TrueTime {
        if (rootDispersionMax > rootDispersionMax) {
            val log = String.format(
                Locale.getDefault(),
                "The recommended max rootDispersion value is %f. You are setting it at %f",
                rootDispersionMax, rootDispersionMax
            )
            TrueLog.w(TAG, log)
        }

        TrueTime.rootDispersionMax = rootDispersionMax
        return INSTANCE
    }

    @Synchronized open fun withServerResponseDelayMax(serverResponseDelayInMillis: Int): TrueTime {
        serverResponseDelayMax = serverResponseDelayInMillis
        return INSTANCE
    }

    @Synchronized fun withNtpHost(ntpHost: String): TrueTime {
        _ntpHost = ntpHost
        return INSTANCE
    }

    @Synchronized open fun withLoggingEnabled(isLoggingEnabled: Boolean): TrueTime {
        TrueLog.setLoggingEnabled(isLoggingEnabled)
        return INSTANCE
    }

    // -----------------------------------------------------------------------------------

    @Throws(IOException::class)
    protected fun initialize(ntpHost: String) {
        if (isInitialized) {
            TrueLog.i(TAG, "---- TrueTime already initialized from previous boot/init")
            return
        }

        requestTime(ntpHost)
        saveTrueTimeInfoToDisk()
    }

    @Throws(IOException::class)
    fun requestTime(ntpHost: String): LongArray {
        return SNTP_CLIENT.requestTime(
            ntpHost,
            rootDelayMax,
            rootDispersionMax,
            serverResponseDelayMax,
            udpSocketTimeoutInMillis
        )
    }

    fun cacheTrueTimeInfo(response: LongArray) {
        SNTP_CLIENT.cacheTrueTimeInfo(response)
    }
}

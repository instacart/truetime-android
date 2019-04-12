package com.instacart.library.truetime

import android.os.SystemClock

internal class DiskCacheClient {

    companion object {
        private val TAG = DiskCacheClient::class.java.simpleName
    }

    private var _cacheInterface: CacheInterface? = null

    // has boot time changed (simple check)
    fun isTrueTimeCachedFromAPreviousBoot(): Boolean {
        if (cacheUnavailable()) {
            return false
        }

        val cachedBootTime = _cacheInterface?.get(CacheInterface.KEY_CACHED_BOOT_TIME, 0L) ?: 0L
        if (cachedBootTime == 0L) {
            return false
        }
        val bootTimeChanged = SystemClock.elapsedRealtime() < getCachedDeviceUptime()
        TrueLog.i(TAG, "---- boot time changed $bootTimeChanged")
        return !bootTimeChanged
    }

    fun getCachedDeviceUptime(): Long = if (cacheUnavailable()) {
        0L
    } else _cacheInterface?.get(CacheInterface.KEY_CACHED_DEVICE_UPTIME, 0L) ?: 0L

    fun getCachedSntpTime(): Long = if (cacheUnavailable()) {
        0L
    } else _cacheInterface?.get(CacheInterface.KEY_CACHED_SNTP_TIME, 0L) ?: 0L

    /**
     * Provide your own cache interface to cache the true time information.
     * @param cacheInterface the customized cache interface to save the true time data.
     */
    fun enableCacheInterface(cacheInterface: CacheInterface) {
        this._cacheInterface = cacheInterface
    }

    /**
     * Clear the cache cache when the device is rebooted.
     * @param cacheInterface the customized cache interface to save the true time data.
     */
    @JvmOverloads fun clearCachedInfo(cacheInterface: CacheInterface? = this._cacheInterface) {
        cacheInterface?.clear()
    }

    fun cacheTrueTimeInfo(sntpClient: SntpClient) {
        if (cacheUnavailable()) {
            return
        }

        val cachedSntpTime = sntpClient.cachedSntpTime
        val cachedDeviceUptime = sntpClient.cachedDeviceUptime
        val bootTime = cachedSntpTime - cachedDeviceUptime

        TrueLog.d(
            TAG,
            String.format(
                "Caching true time info to disk sntp [%s] device [%s] boot [%s]",
                cachedSntpTime,
                cachedDeviceUptime,
                bootTime
            )
        )

        _cacheInterface?.put(CacheInterface.KEY_CACHED_BOOT_TIME, bootTime)
        _cacheInterface?.put(CacheInterface.KEY_CACHED_DEVICE_UPTIME, cachedDeviceUptime)
        _cacheInterface?.put(CacheInterface.KEY_CACHED_SNTP_TIME, cachedSntpTime)
    }

    // -----------------------------------------------------------------------------------

    private fun cacheUnavailable(): Boolean {
        if (_cacheInterface == null) {
            TrueLog.w(TAG, "Cannot use disk caching strategy for TrueTime. CacheInterface unavailable")
            return true
        }
        return false
    }
}

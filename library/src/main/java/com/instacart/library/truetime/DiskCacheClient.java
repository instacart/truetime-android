package com.instacart.library.truetime;

import android.os.SystemClock;

import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_BOOT_ID;
import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_BOOT_TIME;
import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_DEVICE_UPTIME;
import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_SNTP_TIME;

class DiskCacheClient {

    private static final String TAG = DiskCacheClient.class.getSimpleName();

    private CacheInterface _cacheInterface = null;

    /**
     * Provide your own cache interface to cache the true time information.
     * @param cacheInterface the customized cache interface to save the true time data.
     */
    void enableCacheInterface(CacheInterface cacheInterface) {
        this._cacheInterface = cacheInterface;
    }

    void clearCachedInfo() {
        clearCachedInfo(this._cacheInterface);
    }

    /**
     * Clear the cache cache when the device is rebooted.
     * @param cacheInterface the customized cache interface to save the true time data.
     */
    void clearCachedInfo(CacheInterface cacheInterface) {
        if (cacheInterface != null) {
            cacheInterface.clear();
        }
    }

    void cacheTrueTimeInfo(SntpClient sntpClient) {
        if (cacheUnavailable()) {
            return;
        }

        long cachedSntpTime = sntpClient.getCachedSntpTime();
        long cachedDeviceUptime = sntpClient.getCachedDeviceUptime();
        long bootTime = cachedSntpTime - cachedDeviceUptime;

        TrueLog.d(TAG,
                String.format("Caching true time info to disk sntp [%s] device [%s] boot [%s]",
                        cachedSntpTime,
                        cachedDeviceUptime,
                        bootTime));

        _cacheInterface.put(KEY_CACHED_BOOT_TIME, bootTime);
        _cacheInterface.put(KEY_CACHED_DEVICE_UPTIME, cachedDeviceUptime);
        _cacheInterface.put(KEY_CACHED_SNTP_TIME, cachedSntpTime);

    }

    boolean isTrueTimeCachedFromAPreviousBoot() {
        if (cacheUnavailable()) {
            return false;
        }

        long cachedBootTime = _cacheInterface.get(KEY_CACHED_BOOT_TIME, 0L);
        if (cachedBootTime == 0) {
            return false;
        }

        // has boot time changed (simple check)
        boolean bootTimeChanged = SystemClock.elapsedRealtime() < getCachedDeviceUptime();
        TrueLog.i(TAG, "---- boot time changed " + bootTimeChanged);
        return !bootTimeChanged;
    }

    long getCachedDeviceUptime() {
        if (cacheUnavailable()) {
            return 0L;
        }

        return _cacheInterface.get(KEY_CACHED_DEVICE_UPTIME, 0L);
    }

    long getCachedSntpTime() {
        if (cacheUnavailable()) {
            return 0L;
        }

        return _cacheInterface.get(KEY_CACHED_SNTP_TIME, 0L);
    }

    String getCachedBootId() {
        if (cacheUnavailable()) {
            return null;
        }

        return _cacheInterface.get(KEY_CACHED_BOOT_ID, null);
    }

    void cacheBootId(String bootId) {
        if (cacheUnavailable())
            return;

        _cacheInterface.put(KEY_CACHED_BOOT_ID, bootId);
    }

    // -----------------------------------------------------------------------------------

    private boolean cacheUnavailable() {
        if (_cacheInterface == null) {
            TrueLog.w(TAG, "Cannot use disk caching strategy for TrueTime. CacheInterface unavailable");
            return true;
        }
        return false;
    }
}

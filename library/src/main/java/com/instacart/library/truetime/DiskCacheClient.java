package com.instacart.library.truetime;

import android.os.SystemClock;

import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_BOOT_TIME;
import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_DEVICE_UPTIME;
import static com.instacart.library.truetime.CacheInterface.KEY_CACHED_SNTP_TIME;

class DiskCacheClient {

    private static final String TAG = DiskCacheClient.class.getSimpleName();

    private CacheInterface _cacheInterface = null;

    /**
     * Provide your own cache interface to cache the true time information.
     * Please be noted that if you provide such cache interface, it is also your own responsibility
     * to clear the cache on device reboot. This is a must.
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
        if (sharedPreferencesUnavailable()) {
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
        if (sharedPreferencesUnavailable()) {
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
        if (sharedPreferencesUnavailable()) {
            return 0L;
        }

        return _cacheInterface.get(KEY_CACHED_DEVICE_UPTIME, 0L);
    }

    long getCachedSntpTime() {
        if (sharedPreferencesUnavailable()) {
            return 0L;
        }

        return _cacheInterface.get(KEY_CACHED_SNTP_TIME, 0L);
    }

    // -----------------------------------------------------------------------------------

    private boolean sharedPreferencesUnavailable() {
        if (_cacheInterface == null) {
            TrueLog.w(TAG, "Cannot use disk caching strategy for TrueTime. CacheInterface unavailable");
            return true;
        }
        return false;
    }
}

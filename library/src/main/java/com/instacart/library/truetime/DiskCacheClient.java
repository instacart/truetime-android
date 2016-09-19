package com.instacart.library.truetime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import static android.content.Context.MODE_PRIVATE;

class DiskCacheClient {

    private static final String KEY_CACHED_SHARED_PREFS = "com.instacart.library.truetime.shared_preferences";
    private static final String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    private static final String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    private static final String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";

    private static final String TAG = DiskCacheClient.class.getSimpleName();

    private SharedPreferences _sharedPreferences = null;

    void enableDiskCaching(Context context) {
        _sharedPreferences = context.getSharedPreferences(KEY_CACHED_SHARED_PREFS, MODE_PRIVATE);
    }

    void clearCachedInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_CACHED_SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().clear().apply();
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

        _sharedPreferences.edit().putLong(DiskCacheClient.KEY_CACHED_BOOT_TIME, bootTime).apply();
        _sharedPreferences.edit().putLong(DiskCacheClient.KEY_CACHED_DEVICE_UPTIME, cachedDeviceUptime).apply();
        _sharedPreferences.edit().putLong(DiskCacheClient.KEY_CACHED_SNTP_TIME, cachedSntpTime).apply();

    }

    boolean isTrueTimeCachedFromAPreviousBoot() {
        if (sharedPreferencesUnavailable()) {
            return false;
        }

        long cachedBootTime = _sharedPreferences.getLong(DiskCacheClient.KEY_CACHED_BOOT_TIME, 0L);
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

        return _sharedPreferences.getLong(DiskCacheClient.KEY_CACHED_DEVICE_UPTIME, 0L);
    }

    long getCachedSntpTime() {
        if (sharedPreferencesUnavailable()) {
            return 0L;
        }

        return _sharedPreferences.getLong(DiskCacheClient.KEY_CACHED_SNTP_TIME, 0L);
    }

    // -----------------------------------------------------------------------------------

    private boolean sharedPreferencesUnavailable() {
        if (_sharedPreferences == null) {
            TrueLog.w(TAG, "Cannot use disk caching strategy for TrueTime. SharedPreferences unavailable");
            return true;
        }
        return false;
    }
}

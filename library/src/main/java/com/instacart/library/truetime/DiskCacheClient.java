package com.instacart.library.truetime;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

public class DiskCacheClient {

    static final String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    static final String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    static final String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";

    private static final String TAG = DiskCacheClient.class.getSimpleName();

    private SharedPreferences _sharedPreferences = null;

    public void setSource(SharedPreferences sharedPreferences) {
        _sharedPreferences = sharedPreferences;
    }

    void clearCachedInfo(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) {
            return;
        }

        sharedPreferences.edit().remove(KEY_CACHED_BOOT_TIME).apply();
        sharedPreferences.edit().remove(KEY_CACHED_DEVICE_UPTIME).apply();
        sharedPreferences.edit().remove(KEY_CACHED_SNTP_TIME).apply();
    }

    void cacheTrueTimeInfo(SntpClient sntpClient) {
        if (sharedPreferencesUnavailable()) {
            return;
        }

        long cachedSntpTime = sntpClient.getCachedSntpTime();
        long cachedDeviceUptime = sntpClient.getCachedDeviceUptime();
        long bootTime = cachedSntpTime - cachedDeviceUptime;

        Log.d(TAG,
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

        // has boot time changed
        long cachedSntpTime = getCachedSntpTime();
        long cachedDeviceUptime = getCachedDeviceUptime();
        long currentDeviceUptime = SystemClock.elapsedRealtime();
        long nowAccordingToCachedTrueTime = cachedSntpTime + (currentDeviceUptime - cachedDeviceUptime);
        long currentBootTime = nowAccordingToCachedTrueTime - currentDeviceUptime;

        boolean bootTimeSame = currentBootTime == cachedBootTime;

        Log.i(TAG, "boot time changed " + !bootTimeSame);

        return bootTimeSame;
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
            Log.w(TAG, "Cannot use disk caching strategy for TrueTime. SharedPreferences unavailable");
            return true;
        }
        return false;
    }
}

package com.instacart.library.truetime;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private static final String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    private static final String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    private static final String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";
    private static final TrueTime INSTANCE = new TrueTime();

    protected SharedPreferences sharedPreferences = null;
    protected int udpSocketTimeoutInMillis = 30_000;

    private String _ntpHost = "1.us.pool.ntp.org";
    private SntpClient _sntpClient;
    private boolean _sntpInitialized = false;

    public static TrueTime build() {
        return INSTANCE;
    }

    /**
     * @return Date object that returns the current time in the default Timezone
     */
    public static Date now() {
        if (!isInitialized()) {
            throw new IllegalStateException("You need to call init() on TrueTime at least once.");
        }

        long cachedSntpTime = _getCachedSntpTime();
        long cachedDeviceUptime = _getCachedDeviceUptime();
        long deviceUptime = SystemClock.elapsedRealtime();

        long now = cachedSntpTime + (deviceUptime - cachedDeviceUptime);

        long nowSntp = 0L;

        if (INSTANCE._sntpClient != null) {
            nowSntp = INSTANCE._sntpClient.getCachedSntpTime() + deviceUptime -
                      INSTANCE._sntpClient.getCachedDeviceUptime();
        }

        long nowDisk = INSTANCE.sharedPreferences.getLong(KEY_CACHED_SNTP_TIME, 0L) + deviceUptime -
                       INSTANCE.sharedPreferences.getLong(KEY_CACHED_DEVICE_UPTIME, 0L);
        Log.d(TAG,
              String.format(Locale.getDefault(),
                            "now (disk) [%s] now (sntp) [%s] diff [%f]",
                            nowDisk,
                            nowSntp,
                            (nowDisk - nowSntp) / 1000F));

        return new Date(now);
    }

    public static void clearCachedInfo(SharedPreferences preferences) {
        if (preferences == null) {
            return;
        }
        preferences.edit().remove(KEY_CACHED_BOOT_TIME).apply();
        preferences.edit().remove(KEY_CACHED_DEVICE_UPTIME).apply();
        preferences.edit().remove(KEY_CACHED_SNTP_TIME).apply();
    }

    public static boolean isInitialized() {
        return _isSntpInitialized() || _isTrueTimeCachedFromAPreviousBoot();
    }

    /**
     * Cache TrueTime initialization information in SharedPreferences
     * This can help avoid additional TrueTime initialization on app kills
     */
    public synchronized TrueTime withSharedPreferences(SharedPreferences preferences) {
        sharedPreferences = preferences;
        INSTANCE.sharedPreferences = preferences;
        return INSTANCE;
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        udpSocketTimeoutInMillis = timeoutInMillis;
        INSTANCE.udpSocketTimeoutInMillis = timeoutInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return INSTANCE;
    }

    public void initialize() throws IOException {
        initialize(_ntpHost);
        cacheTrueTimeInfo(sharedPreferences);
    }

    // -----------------------------------------------------------------------------------

    protected void initialize(String ntpHost) throws IOException {
        if (isInitialized()) {
            Log.i(TAG, "---- TrueTime already initialized from previous boot/init");
            Log.i(TAG,
                  String.format(Locale.getDefault(),
                                "Sntp initialized [%b], TrueTime Info in disk [%b]",
                                _isSntpInitialized(),
                                _isTrueTimeCachedFromAPreviousBoot()));
            return;
        }

        SntpClient sntpClient = new SntpClient();
        try {
            sntpClient.requestTime(ntpHost, udpSocketTimeoutInMillis);
            Log.i(TAG, "---- SNTP request successful");
            _setSntpClient(sntpClient);
        } catch (IOException e) {
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
            _sntpInitialized = false;
            throw e;
        }
    }

    protected synchronized static void cacheTrueTimeInfo(SharedPreferences preferences) {
        if (preferences == null) {
            Log.d(TAG, "Preferences unavailable. cannot utilize disk info");
            return;
        }

        long cachedSntpTime = _getCachedSntpTime();
        long cachedDeviceUptime = _getCachedDeviceUptime();
        long bootTime = cachedSntpTime - cachedDeviceUptime;

        Log.d(TAG,
              String.format("Caching true time info to disk sntp [%s] device [%s] boot [%s]",
                            cachedSntpTime,
                            cachedDeviceUptime,
                            bootTime));

        preferences.edit().putLong(KEY_CACHED_BOOT_TIME, bootTime).apply();
        preferences.edit().putLong(KEY_CACHED_DEVICE_UPTIME, cachedDeviceUptime).apply();
        preferences.edit().putLong(KEY_CACHED_SNTP_TIME, cachedSntpTime).apply();
    }

    // -----------------------------------------------------------------------------------

    private synchronized static void _setSntpClient(SntpClient sntpClient) {
        INSTANCE._sntpClient = sntpClient;
        INSTANCE._sntpInitialized = true;
    }

    private static boolean _isSntpInitialized() {
        return INSTANCE._sntpInitialized;
    }

    private static boolean _isTrueTimeCachedFromAPreviousBoot() {
        SharedPreferences preferences = INSTANCE.sharedPreferences;

        if (preferences == null) {
            return false;
        }

        long cachedBootTime = preferences.getLong(KEY_CACHED_BOOT_TIME, 0L);
        if (cachedBootTime == 0) {
            return false;
        }

        // has boot time changed
        long cachedSntpTime = INSTANCE.sharedPreferences.getLong(KEY_CACHED_SNTP_TIME, 0L);
        long cachedDeviceUptime = INSTANCE.sharedPreferences.getLong(KEY_CACHED_DEVICE_UPTIME, 0L);
        long deviceUptime = SystemClock.elapsedRealtime();
        long nowAccordingToCachedTrueTime = cachedSntpTime + (deviceUptime - cachedDeviceUptime);
        long currentBootTime = nowAccordingToCachedTrueTime - deviceUptime;

        boolean bootTimeSame = currentBootTime == cachedBootTime;
        Log.i(TAG, "boot time changed " + !bootTimeSame);

        return bootTimeSame;
    }

    private static long _getCachedDeviceUptime() {
        if (_isSntpInitialized()) {
            return INSTANCE._sntpClient.getCachedDeviceUptime();
        }

        long cachedDeviceUptime = INSTANCE.sharedPreferences.getLong(KEY_CACHED_DEVICE_UPTIME, 0L);

        if (cachedDeviceUptime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedDeviceUptime;
    }

    private static long _getCachedSntpTime() {
        if (_isSntpInitialized()) {
            return INSTANCE._sntpClient.getCachedSntpTime();
        }

        long cachedSntpTime = INSTANCE.sharedPreferences.getLong(KEY_CACHED_SNTP_TIME, 0L);

        if (cachedSntpTime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedSntpTime;
    }
}
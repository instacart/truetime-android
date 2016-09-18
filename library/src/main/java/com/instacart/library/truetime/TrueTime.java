package com.instacart.library.truetime;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private static final TrueTime INSTANCE = new TrueTime();
    private static final SntpClient SNTP_CLIENT = new SntpClient();
    private static final DiskCacheClient DISK_CACHE_CLIENT = new DiskCacheClient();

    private static int _udpSocketTimeoutInMillis = 30_000;

    private String _ntpHost = "1.us.pool.ntp.org";

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

        // -----------------------------------------------------------------------------------
        // hacking
        long nowSntp = (getSntpClient() != null) ? getSntpClient().getCachedSntpTime() + deviceUptime -
                                                   getSntpClient().getCachedDeviceUptime() : 0L;
        long nowDisk = getDiskCacheClient().getCachedSntpTime() + deviceUptime -
                       getDiskCacheClient().getCachedDeviceUptime();
        Log.d(TAG,
              String.format(Locale.getDefault(),
                            "now (disk) [%s] now (sntp) [%s] diff [%f]",
                            nowDisk,
                            nowSntp,
                            (nowDisk - nowSntp) / 1000F));
        // -----------------------------------------------------------------------------------

        return new Date(now);
    }

    public static boolean isInitialized() {
        //hacking
        Log.d("kg", "SNTP initialized " + _isSntpInitialized() + " disk info available " + getDiskCacheClient().isTrueTimeCachedFromAPreviousBoot());
        return _isSntpInitialized() || getDiskCacheClient().isTrueTimeCachedFromAPreviousBoot();
        return SNTP_CLIENT.wasInitialized() || DISK_CACHE_CLIENT.isTrueTimeCachedFromAPreviousBoot();
    }

    public static TrueTime build() {
        return INSTANCE;
    }

    public static void clearCachedInfo(SharedPreferences preferences) {
        DISK_CACHE_CLIENT.clearCachedInfo(preferences);
    }

    public void initialize() throws IOException {
        initialize(_ntpHost);
        cacheTrueTimeInfo();
    }

    /**
     * Cache TrueTime initialization information in SharedPreferences
     * This can help avoid additional TrueTime initialization on app kills
     */
    public synchronized TrueTime withSharedPreferences(SharedPreferences preferences) {
        DISK_CACHE_CLIENT.setSource(preferences);
        return INSTANCE;
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        _udpSocketTimeoutInMillis = timeoutInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------

    protected void initialize(String ntpHost) throws IOException {
        if (isInitialized()) {
            Log.i(TAG, "---- TrueTime already initialized from previous boot/init");
            return;
        }
        SNTP_CLIENT.requestTime(ntpHost, _udpSocketTimeoutInMillis);
    }

    protected synchronized static void cacheTrueTimeInfo() {
        if (!SNTP_CLIENT.wasInitialized()) {
            Log.w(TAG, "SNTP client info not available. cannot cache TrueTime info in disk");
            return;
        }

        SntpClient sntpClient = SNTP_CLIENT;
        DISK_CACHE_CLIENT.cacheTrueTimeInfo(sntpClient);
    }

    private static long _getCachedDeviceUptime() {
        long cachedDeviceUptime = SNTP_CLIENT.wasInitialized()
                                  ? SNTP_CLIENT.getCachedDeviceUptime()
                                  : DISK_CACHE_CLIENT.getCachedDeviceUptime();

        if (cachedDeviceUptime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedDeviceUptime;
    }

    private static long _getCachedSntpTime() {
        long cachedSntpTime = SNTP_CLIENT.wasInitialized()
                              ? SNTP_CLIENT.getCachedSntpTime()
                              : DISK_CACHE_CLIENT.getCachedSntpTime();

        if (cachedSntpTime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedSntpTime;
    }
}
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

    private final DiskCacheClient _diskCacheClient = new DiskCacheClient();
    private String _ntpHost = "1.us.pool.ntp.org";
    private SntpClient _sntpClient;
    private boolean _sntpInitialized = false;
    private int _udpSocketTimeoutInMillis = 30_000;

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
    }

    public static TrueTime build() {
        return INSTANCE;
    }

    public static void clearCachedInfo(SharedPreferences preferences) {
        getDiskCacheClient().clearCachedInfo(preferences);
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
        getDiskCacheClient().setSource(preferences);
        return INSTANCE;
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        INSTANCE._udpSocketTimeoutInMillis = timeoutInMillis;
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

        try {

            SntpClient sntpClient = new SntpClient();
            sntpClient.requestTime(ntpHost, INSTANCE._udpSocketTimeoutInMillis);
            Log.i(TAG, "---- SNTP request successful");
            _setSntpClient(sntpClient);

        } catch (IOException e) {
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
            _sntpInitialized = false;
            throw e;
        }
    }

    protected synchronized static void cacheTrueTimeInfo() {
        if (!_isSntpInitialized()) {
            Log.w(TAG, "SNTP client info not available. cannot cache TrueTime info in disk");
            return;
        }

        SntpClient sntpClient = getSntpClient();
        getDiskCacheClient().cacheTrueTimeInfo(sntpClient);
    }

    private static DiskCacheClient getDiskCacheClient() {
        return INSTANCE._diskCacheClient;
    }

    private static SntpClient getSntpClient() {
        return INSTANCE._sntpClient;
    }

    private synchronized static void _setSntpClient(SntpClient sntpClient) {
        INSTANCE._sntpClient = sntpClient;
        INSTANCE._sntpInitialized = true;
    }

    private static boolean _isSntpInitialized() {
        return getSntpClient() != null && INSTANCE._sntpInitialized;
    }

    private static long _getCachedDeviceUptime() {
        long cachedDeviceUptime = _isSntpInitialized()
                                  ? getSntpClient().getCachedDeviceUptime()
                                  : getDiskCacheClient().getCachedDeviceUptime();

        if (cachedDeviceUptime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedDeviceUptime;
    }

    private static long _getCachedSntpTime() {
        long cachedSntpTime = _isSntpInitialized()
                              ? getSntpClient().getCachedSntpTime()
                              : getDiskCacheClient().getCachedSntpTime();

        if (cachedSntpTime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn't find it.");
        }

        return cachedSntpTime;
    }
}
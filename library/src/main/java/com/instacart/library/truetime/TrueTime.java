package com.instacart.library.truetime;

import android.content.Context;
import android.os.SystemClock;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private static final TrueTime INSTANCE = new TrueTime();
    private static final DiskCacheClient DISK_CACHE_CLIENT = new DiskCacheClient();
    private static final SntpClient SNTP_CLIENT = new SntpClient();

    private static float _rootDelayMax = 100;
    private static float _rootDispersionMax = 100;
    private static int _serverResponseDelayMax = 750;
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

        return new Date(now);
    }

    public static boolean isInitialized() {
        return SNTP_CLIENT.wasInitialized() || DISK_CACHE_CLIENT.isTrueTimeCachedFromAPreviousBoot();
    }

    public static TrueTime build() {
        return INSTANCE;
    }

    public void initialize() throws IOException {
        initialize(_ntpHost);
    }

    /**
     * Cache TrueTime initialization information in SharedPreferences
     * This can help avoid additional TrueTime initialization on app kills
     */
    public synchronized TrueTime withSharedPreferencesCache(Context context) {
        DISK_CACHE_CLIENT.enableCacheInterface(new SharedPreferenceCacheImpl(context));
        return INSTANCE;
    }

    /**
     * Customized TrueTime Cache implementation.
     */
    public synchronized TrueTime withCustomizedCache(CacheInterface cacheInterface) {
        DISK_CACHE_CLIENT.enableCacheInterface(cacheInterface);
        return INSTANCE;
    }

    /**
     * clear the cached TrueTime info on device reboot.
     */
    public static void clearCachedInfo() {
        DISK_CACHE_CLIENT.clearCachedInfo();
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        _udpSocketTimeoutInMillis = timeoutInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withRootDelayMax(float rootDelayMax) {
        if (rootDelayMax > _rootDelayMax) {
          String log = String.format(Locale.getDefault(),
              "The recommended max rootDelay value is %f. You are setting it at %f",
              _rootDelayMax, rootDelayMax);
          TrueLog.w(TAG, log);
        }

        _rootDelayMax = rootDelayMax;
        return INSTANCE;
    }

    public synchronized TrueTime withRootDispersionMax(float rootDispersionMax) {
      if (rootDispersionMax > _rootDispersionMax) {
        String log = String.format(Locale.getDefault(),
            "The recommended max rootDispersion value is %f. You are setting it at %f",
            _rootDispersionMax, rootDispersionMax);
        TrueLog.w(TAG, log);
      }

      _rootDispersionMax = rootDispersionMax;
      return INSTANCE;
    }

    public synchronized TrueTime withServerResponseDelayMax(int serverResponseDelayInMillis) {
        _serverResponseDelayMax = serverResponseDelayInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return INSTANCE;
    }

    public synchronized TrueTime withLoggingEnabled(boolean isLoggingEnabled) {
        TrueLog.setLoggingEnabled(isLoggingEnabled);
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------

    protected void initialize(String ntpHost) throws IOException {
        if (isInitialized()) {
            TrueLog.i(TAG, "---- TrueTime already initialized from previous boot/init");
            return;
        }

        requestTime(ntpHost);
        saveTrueTimeInfoToDisk();
    }

    long[] requestTime(String ntpHost) throws IOException {
        return SNTP_CLIENT.requestTime(ntpHost,
            _rootDelayMax,
            _rootDispersionMax,
            _serverResponseDelayMax,
            _udpSocketTimeoutInMillis);
    }

    synchronized static void saveTrueTimeInfoToDisk() {
        if (!SNTP_CLIENT.wasInitialized()) {
            TrueLog.i(TAG, "---- SNTP client not available. not caching TrueTime info in disk");
            return;
        }
        DISK_CACHE_CLIENT.cacheTrueTimeInfo(SNTP_CLIENT);
    }

    void cacheTrueTimeInfo(long[] response) {
        SNTP_CLIENT.cacheTrueTimeInfo(response);
    }

    private static long _getCachedDeviceUptime() {
        long cachedDeviceUptime = SNTP_CLIENT.wasInitialized()
                                  ? SNTP_CLIENT.getCachedDeviceUptime()
                                  : DISK_CACHE_CLIENT.getCachedDeviceUptime();

        if (cachedDeviceUptime == 0L) {
            throw new RuntimeException("expected device time from last boot to be cached. couldn't find it.");
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

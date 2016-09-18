package com.instacart.library.truetime;

import android.os.SystemClock;

import java.io.IOException;
import java.util.Date;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();
    private static final TrueTime INSTANCE = new TrueTime();

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

        long now = INSTANCE._sntpClient.getCachedSntpTime()//
                   + SystemClock.elapsedRealtime() -
                   INSTANCE._sntpClient.getCachedDeviceUptime(); // elapsed deviceUptime from calc.

        return new Date(now);
    }

    public static boolean isInitialized() {
        return INSTANCE._sntpInitialized;
    }

    // -----------------------------------------------------------------------------------

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        INSTANCE.udpSocketTimeoutInMillis = timeoutInMillis;
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

    public void initialize() {
        SntpClient sntpClient = new SntpClient();

        try {
            sntpClient.requestTime(INSTANCE._ntpHost, INSTANCE.udpSocketTimeoutInMillis);
            TrueLog.i(TAG, "---- SNTP request successful");
            setSntpClient(sntpClient);
        } catch (IOException e) {
            TrueLog.e(TAG, "TrueTime initialization failed", new Throwable(e));
            _sntpInitialized = false;
        }
    }

    // -----------------------------------------------------------------------------------

    protected synchronized static void setSntpClient(SntpClient sntpClient) {
        INSTANCE._sntpClient = sntpClient;
        INSTANCE._sntpInitialized = true;
    }
}
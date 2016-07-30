package com.instacart.library.truetime;

import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Date;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private static final TrueTime INSTANCE = new TrueTime();

    private String _ntpHost = "1.us.pool.ntp.org";
    private SntpClient _sntpClient = null;
    private int _udpSocketTimeoutInMillis = 30_000;

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


    // -----------------------------------------------------------------------------------

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        _udpSocketTimeoutInMillis = timeoutInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return INSTANCE;
    }

    public void initialize() {
        SntpClient sntpClient = new SntpClient();

        try {
            sntpClient.requestTime(INSTANCE._ntpHost, INSTANCE._udpSocketTimeoutInMillis);
            Log.i(TAG, "---- SNTP request successful");
            setSntpClient(sntpClient);
        } catch (IOException e) {
            _sntpClient = null;
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
        }
    }

    // -----------------------------------------------------------------------------------

    protected synchronized static void setSntpClient(SntpClient sntpClient) {
        INSTANCE._sntpClient = sntpClient;
    }

    protected static int getUdpSocketTimeout() {
        return INSTANCE._udpSocketTimeoutInMillis;
    }
}
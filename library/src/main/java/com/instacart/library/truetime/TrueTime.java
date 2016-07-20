package com.instacart.library.truetime;

import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Date;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    protected static TrueTime instance;

    private String _ntpHost = "1.us.pool.ntp.org";
    private SntpClient _sntpClient;
    private boolean _sntpInitialized = false;
    private int _udpSocketTimeoutInMillis = 30_000;

    protected TrueTime() { }

    public static TrueTime build() {
        instance = new TrueTime();
        return instance;
    }

    /**
     * @return Date object that returns the current time in the default Timezone
     */
    public static Date now() {
        if (!instance._sntpInitialized) {
            throw new IllegalStateException("You need to call init() on TrueTime at least once.");
        }

        long now = instance._sntpClient.getCachedSntpTime()//
                   + SystemClock.elapsedRealtime() -
                   instance._sntpClient.getCachedDeviceUptime(); // elapsed deviceUptime from calc.

        return new Date(now);
    }

    // -----------------------------------------------------------------------------------

    public TrueTime withConnectionTimeout(int timeoutInMillis) {
        assertInstanceCreated();

        instance._udpSocketTimeoutInMillis = timeoutInMillis;
        return instance;
    }

    public TrueTime withNtpHost(String ntpHost) {
        assertInstanceCreated();

        instance._ntpHost = ntpHost;
        return instance;
    }

    public void initClient() {
        SntpClient sntpClient = new SntpClient();

        try {
            sntpClient.requestTime(instance._ntpHost, instance._udpSocketTimeoutInMillis);
            Log.i(TAG, "---- SNTP request successful");
            setSntpClient(sntpClient);
        } catch (IOException e) {
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
            instance._sntpInitialized = false;
        }
    }

    // -----------------------------------------------------------------------------------

    protected static void setSntpClient(SntpClient sntpClient) {
        instance._sntpClient = sntpClient;
        instance._sntpInitialized = true;
    }

    protected static int getUdpSocketTimeout() {
        return instance._udpSocketTimeoutInMillis;
    }

    private void assertInstanceCreated() {
        if (instance == null) {
            throw new IllegalStateException("call build before you start");
        }
    }

}
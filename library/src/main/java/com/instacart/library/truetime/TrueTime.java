package com.instacart.library.truetime;

import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Date;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private String _ntpHost = "1.us.pool.ntp.org";
    private SntpClient _sntpClient;
    private boolean _sntpInitialized = false;
    private int _udpSocketTimeout = 30000;

    private TrueTime() {
        // no public constructors
    }

    public static TrueTime get() {
        return new TrueTime();
    }

    public TrueTime setConnectionTimeout(int timeout) {
        _udpSocketTimeout = timeout;
        return this;
    }

    public TrueTime setDefaultNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return this;
    }

    /**
     * Client should let TrueTime know of device reboots
     * This can be done with typically using a Broadcast Receiver
     */
    public void deviceWasRebooted() {
        _sntpInitialized = false;
    }

    /**
     * @return Date object that returns the current time in the default Timezone
     */
    public Date now() {
        if (!_sntpInitialized) {
            throw new IllegalStateException("You need to call init() on TrueTime atleast once.");
        }

        long now = _sntpClient.getNtpTime()//
                   + SystemClock.elapsedRealtime() - _sntpClient.getDeviceUptime(); // elapsed deviceUptime from calc.

        return new Date(now);
    }

    public void initClient() {
        _sntpClient = new SntpClient();

        try {
            _sntpClient.requestTime(_ntpHost, _udpSocketTimeout);
            Log.i(TAG, "---- SNTP request successful");
            _sntpInitialized = true;
        } catch (IOException e) {
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
            _sntpInitialized = false;
        }
    }
}
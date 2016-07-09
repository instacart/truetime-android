package com.instacart.library.truetime;

import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();

    private SntpClient _sntpClient;
    private boolean _sntpInitialized = false;
    private TimeZone _timeZone;
    private int _udpSocketTimeout = 30000;

    private TrueTime() {
        _timeZone = TimeZone.getDefault();
        _initClient();
    }

    public static TrueTime get() {
        return new TrueTime();
    }

    public TrueTime setTimeZone(TimeZone timeZone) {
        _timeZone = timeZone;
        return this;
    }

    public TrueTime setConnectionTimeout(int timeout) {
        _udpSocketTimeout = timeout;
        return this;
    }

    /**
     * @return Date object that returns the current time in the default Timezone
     */
    public Date now() {

        if (!_sntpInitialized) {
            _initClient();
        }

        long now = _sntpClient.getNtpTime() +
                   SystemClock.elapsedRealtime() - _sntpClient.getDeviceUptime(); // device uptime elapsed difference

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(_timeZone);
        cal.setTimeInMillis(now);
        return cal.getTime();
    }

    private void _initClient() {
        _sntpClient = new SntpClient();
        try {
            _sntpClient.requestTime("0.us.pool.ntp.org", _udpSocketTimeout);
            _sntpInitialized = true;
        } catch (IOException e) {
            Log.e(TAG, "TrueTime initialization failed", new Throwable(e));
            _sntpInitialized = false;
        }
    }
}
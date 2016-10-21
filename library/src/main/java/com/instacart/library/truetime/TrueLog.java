package com.instacart.library.truetime;

import android.util.Log;

class TrueLog {

    private static boolean LOGGING_ENABLED = false;

    static void v(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.v(tag, msg);
        }
    }

    static void d(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.d(tag, msg);
        }
    }

    static void i(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.i(tag, msg);
        }
    }

    static void w(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg);
        }
    }

    static void w(String tag, String msg, Throwable t) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg, t);
        }
    }

    static void e(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg);
        }
    }

    static void e(String tag, String msg, Throwable t) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg, t);
        }
    }

    static void wtf(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg);
        }
    }

    static void wtf(String tag, String msg, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg, tr);
        }
    }

    static void setLoggingEnabled(boolean isLoggingEnabled) {
        LOGGING_ENABLED = isLoggingEnabled;
    }
}

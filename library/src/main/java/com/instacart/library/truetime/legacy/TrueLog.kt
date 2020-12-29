package com.instacart.library.truetime.legacy

import android.util.Log

object TrueLog {

    private var LOGGING_ENABLED = false

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String, t: Throwable) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg, t)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String, t: Throwable) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg, t)
        }
    }

    @JvmStatic
    fun wtf(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg)
        }
    }

    @JvmStatic
    fun wtf(tag: String, msg: String, tr: Throwable) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg, tr)
        }
    }

    @JvmStatic
    fun setLoggingEnabled(isLoggingEnabled: Boolean) {
        LOGGING_ENABLED = isLoggingEnabled
    }
}

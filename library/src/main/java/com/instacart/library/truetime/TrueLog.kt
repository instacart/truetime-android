package com.instacart.library.truetime

import android.util.Log

object TrueLog {

    private var LOGGING_ENABLED = false

    fun v(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg)
        }
    }

    fun w(tag: String, msg: String, t: Throwable) {
        if (LOGGING_ENABLED) {
            Log.w(tag, msg, t)
        }
    }

    fun e(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg)
        }
    }

    fun e(tag: String, msg: String, t: Throwable) {
        if (LOGGING_ENABLED) {
            Log.e(tag, msg, t)
        }
    }

    fun wtf(tag: String, msg: String) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg)
        }
    }

    fun wtf(tag: String, msg: String, tr: Throwable) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg, tr)
        }
    }

    fun setLoggingEnabled(isLoggingEnabled: Boolean) {
        LOGGING_ENABLED = isLoggingEnabled
    }
}

package com.instacart.library.truetime

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent

class BootCompletedBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = BootCompletedBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        TrueLog.i(TAG, "---- clearing TrueTime disk cache as we've detected a boot")
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            TrueTime.clearCachedInfo()
        }
    }
}

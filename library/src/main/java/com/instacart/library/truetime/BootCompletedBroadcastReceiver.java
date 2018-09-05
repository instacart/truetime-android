package com.instacart.library.truetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedBroadcastReceiver
      extends BroadcastReceiver {

    private static final String TAG = BootCompletedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        TrueLog.i(TAG, "---- clearing TrueTime disk cache as we've detected a boot");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TrueTime.clearCachedInfo();
        }
    }
}

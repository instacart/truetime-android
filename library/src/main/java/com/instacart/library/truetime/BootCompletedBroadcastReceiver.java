package com.instacart.library.truetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedBroadcastReceiver
      extends BroadcastReceiver {

    private static final String TAG = BootCompletedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        TrueLog.i(TAG, "---- clearing TrueTime disk cache as we've detected a boot");
        TrueTime.clearCachedInfo(context);
    }
}

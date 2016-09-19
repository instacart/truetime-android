package com.instacart.library.truetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupCompletedBroadcastReceiver
      extends BroadcastReceiver {

    private static final String TAG = BootupCompletedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "---- clearing TrueTime disk cache as we've detected a boot");
        TrueTime.clearCachedInfo(context);
    }
}

package com.instacart.library.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.instacart.library.truetime.TrueTime;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TrueTime.clearCachedInfo(new CustomizedSharedPreferenceCacheImpl(context));
    }
}

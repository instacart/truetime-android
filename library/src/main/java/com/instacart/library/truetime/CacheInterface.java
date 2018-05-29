package com.instacart.library.truetime;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface CacheInterface {

    String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";

    void put(@TrueTimeCacheType String key, long value);

    long get(@TrueTimeCacheType String key, long defaultValue);

    void clear();

    @StringDef({KEY_CACHED_BOOT_TIME,
            KEY_CACHED_DEVICE_UPTIME,
            KEY_CACHED_SNTP_TIME
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface TrueTimeCacheType {
    }
}

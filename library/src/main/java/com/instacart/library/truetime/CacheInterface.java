package com.instacart.library.truetime;

public interface CacheInterface {

    String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";

    void put(String key, long value);

    long get(String key, long defaultValue);

    void clear();


}

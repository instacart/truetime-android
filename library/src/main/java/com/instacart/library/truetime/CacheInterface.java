package com.instacart.library.truetime;

public interface CacheInterface {

    String KEY_CACHED_BOOT_TIME = "com.instacart.library.truetime.cached_boot_time";
    String KEY_CACHED_DEVICE_UPTIME = "com.instacart.library.truetime.cached_device_uptime";
    String KEY_CACHED_SNTP_TIME = "com.instacart.library.truetime.cached_sntp_time";
    String KEY_CACHED_BOOT_ID = "com.instacart.library.truetime.cached_boot_id";

    void put(String key, long value);

    void put(String key, String value);

    long get(String key, long defaultValue);

    String get(String key, String defaultValue);

    void clear();


}

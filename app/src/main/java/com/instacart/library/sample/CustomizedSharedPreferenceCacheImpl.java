package com.instacart.library.sample;

import android.content.Context;
import android.content.SharedPreferences;

import com.instacart.library.truetime.CacheInterface;

import static android.content.Context.MODE_PRIVATE;

class CustomizedSharedPreferenceCacheImpl implements CacheInterface {

    private static final String KEY_CACHED_SHARED_PREFS = "my_customized_shared_preference";

    private SharedPreferences _sharedPreferences = null;

    public CustomizedSharedPreferenceCacheImpl(Context context) {
        _sharedPreferences = context.getSharedPreferences(KEY_CACHED_SHARED_PREFS, MODE_PRIVATE);
    }

    @Override
    public void put(String key, long value) {
        _sharedPreferences.edit().putLong(key, value).apply();
    }

    @Override
    public long get(String key, long defaultValue) {
        return _sharedPreferences.getLong(key, defaultValue);
    }

    @Override
    public void clear() {
        remove(CacheInterface.KEY_CACHED_BOOT_TIME);
        remove(CacheInterface.KEY_CACHED_DEVICE_UPTIME);
        remove(CacheInterface.KEY_CACHED_SNTP_TIME);
    }

    private void remove(String keyCachedBootTime) {
        _sharedPreferences.edit().remove(keyCachedBootTime).apply();
    }
}

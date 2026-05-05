# Keep TrueTime public API
-keep public interface com.instacart.truetime.time.TrueTime { *; }
-keep public class com.instacart.truetime.time.TrueTimeImpl { *; }
-keep public class com.instacart.truetime.time.TrueTimeParameters { *; }
-keep public class com.instacart.truetime.time.TrueTimeParameters$Builder { *; }
-keep public interface com.instacart.truetime.CacheProvider { *; }
-keep public class com.instacart.truetime.BasicCacheProvider { *; }
-keep public interface com.instacart.truetime.TrueTimeEventListener { *; }
-keep public class com.instacart.truetime.sntp.SntpResult { *; }
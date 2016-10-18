# TrueTime for Android

![TrueTime](truetime.png "TrueTime for Android")

*Make sure to check out our counterpart too: [TrueTime](https://github.com/instacart/TrueTime.swift), an SNTP library for Swift.*

NTP client for Android. Calculate the date and time "now" impervious to manual changes to device clock time.

In certain applications it becomes important to get the real or "true" date and time. On most devices, if the clock has been changed manually, then a `new Date()` instance gives you a time impacted by local settings.

Users may do this for a variety of reasons, like being in different timezones, trying to be punctual by setting their clocks 5 – 10 minutes early, etc. Your application or service may want a date that is unaffected by these changes and reliable as a source of truth. TrueTime gives you that.

You can read more about the use case in our [blog post](https://tech.instacart.com/truetime/).

# How is TrueTime calculated?

It's pretty simple actually. We make a request to an NTP server that gives us the actual time. We then establish the delta between device uptime and uptime at the time of the network response. Each time "now" is requested subsequently, we account for that offset and return a corrected `Date` object.

Also, once we have this information it's valid until the next time you boot your device. This means if you enable the disk caching feature, after a single successfull NTP request you can use the information on disk directly without ever making another network request. This applies even across application kills which can happen frequently if your users have a memory starved device.

# Installation

We use [Jitpack](https://jitpack.io) to host the library.

Add this to your application's `build.gradle` file:

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    // ...
    compile 'com.github.instacart.truetime-android:library-extension-rx:<release-version>'

    // or if you want the vanilla version of Truetime:
    compile 'com.github.instacart.truetime-android:library:<release-version>'
}
```

# Usage

## Vanilla version

Importing `'com.github.instacart.truetime-android:library:<release-version>'` should be sufficient for this.

```java
TrueTime.build().initialize();
```

`initialize` must be run on a background thread. If you run it on the main thread, you will get a [`NetworkOnMainThreadException`](https://developer.android.com/reference/android/os/NetworkOnMainThreadException.html)

You can then use:

```java
Date noReallyThisIsTheTrueDateAndTime = TrueTime.now();
```

... #winning

## Rx-ified Version

If you're down to using [RxJava](https://github.com/ReactiveX/RxJava) then we go all the way and implement the full NTP. Use the nifty `initializeRx()` api which takes in an NTP pool server host.

```java
TrueTimeRx.build()
        .initializeRx("0.north-america.pool.ntp.org")
        .subscribeOn(Schedulers.io())
        .subscribe(date -> {
            Log.v(TAG, "TrueTime was initialized and we have a time: " + date);
        }, throwable -> {
            throwable.printStackTrace();
        });
```

Now, as before:

```java
TrueTimeRx.now(); // return a Date object with the "true" time.
```

### What is nifty about the Rx version?

* as against just SNTP, you get full NTP (read: far more accurate time)
* the NTP pool address you provide is resolved into multiple IP addresses
* we query each IP multiple times, guarding against checks, and taking the best response
* if any one of the requests fail, we retry that failed request (alone) for a specified number of times
* we collect all the responses and again filter for the best result as per the NTP spec

## Notes/tips:

* Each `initialize` call makes an SNTP network request. TrueTime needs to be `initialize`d only once ever, per device boot. Use TrueTime's `withSharedPreferences` option to make use of this feature and avoid repeated network request calls.
* Preferable use dependency injection (like [Dagger](http://square.github.io/dagger/)) and create a TrueTime @Singleton object
* You can read up on Wikipedia the differences between [SNTP](https://en.wikipedia.org/wiki/Network_Time_Protocol#SNTP) and [NTP](https://www.meinbergglobal.com/english/faq/faq_37.htm).
* TrueTime is also [available for iOS/Swift](https://github.com/instacart/truetime.swift)

## Exception handling:

* an `InvalidNtpServerResponseException` is thrown every time the server gets an invalid response (this can happen with the individual SNTP calls).
* If TrueTime fails to initialize (because of the above exception being throw), then an `IllegalStateException` is thrown if you try to call `TrueTime.now()` at a later point.

# License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

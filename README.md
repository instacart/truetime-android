# TrueTime for Android

![TrueTime](truetime.png "TrueTime for Android")

SNTP client for Android. Calculate the date and time "now" impervious to manual changes to device clock time.

In certain applications it becomes important to get the real or "true" date and time. On most devices, if the clock has been changed manually, then a `new Date()` instance gives you a time impacted by local settings.

Users may do this for a variety of reasons, like being in different timezones, trying to be punctual by setting their clocks 5 – 10 minutes early, etc. Your application or service may want a date that is unaffected by these changes and reliable as a source of truth. TrueTime gives you that.

You can read more about the use case in our [blog post](https://tech.instacart.com/truetime/).

# How is TrueTime calculated?

It's pretty simple actually. We make a request to an NTP server that gives us the actual time. We then establish the delta between device uptime and uptime at the time of the network response. Each time "now" is requested subsequently, we account for that offset and return a corrected `Date` object.

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
    compile 'com.github.instacart.truetime-android:library-extension-rx:1.0'

    // or if you want the vanilla version of Truetime:
    compile 'com.github.instacart.truetime-android:library:1.0'
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

If you're down to using [RxJava](https://github.com/ReactiveX/RxJava) then there's a niftier `initialize()` api that takes in the pool of hosts you want to query.

```java
List<String> ntpHosts = Arrays.asList("0.north-america.pool.ntp.org",
                                      "1.north-america.pool.ntp.org");
TrueTimeRx.build()
        .initialize(ntpHosts)
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

* it can take in multiple SNTP hosts to shoot out the UDP request
* those UDP requests are executed in parallel
* if one of the SNTP requests fail, we retry the failed request (alone) for a specified number of times
* as soon as we hear back from any of the hosts, we immediately take that and terminate the rest of the requests

## Notes/tips:

* You want to `initialize` only once per device restart. This means as long as your device is not restarted, TrueTime needs to be initialized only once.
* Preferable use dependency injection (like [Dagger](http://square.github.io/dagger/)) and create a TrueTime @Singleton object
* TrueTime was built to be accurate "enough", hence the use of [SNTP](https://en.wikipedia.org/wiki/Network_Time_Protocol#SNTP). If you need exact millisecond accuracy then you probably want [NTP](https://www.meinbergglobal.com/english/faq/faq_37.htm) (i.e. SNTP + statistical analysis to ensure the reference time is exactly correct). TrueTime provides the building blocks for this. We welcome PRs if you think you can do this with TrueTime(Rx) pretty easily :).
* TrueTime is also [available for iOS/Swift](https://github.com/instacart/truetime.swift)

## Exception handling:

* an `InvalidNtpServerResponseException` is thrown every time the server gets an invalid response (this can happen with the SNTP calls).
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

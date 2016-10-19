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

## Troubleshootig/Exception handling:

When you execute the TrueTime initialization, you are very highly likely to get an `InvalidNtpServerResponseException` because of root delay violation or  root dipsersion violation the first time. This is an expected occurrence as per the [NTP Spec](https://tools.ietf.org/html/rfc5905) and needs to be handled.

### Why does this happen?

The NTP protocol works on [UDP](https://en.wikipedia.org/wiki/User_Datagram_Protocol): 

> It has no handshaking dialogues, and thus exposes the user's program to any unreliability of the underlying network and so there is no guarantee of delivery, ordering, or duplicate protection
> UDP is suitable for purposes where error checking and correction is either not necessary or is *performed in the application*, avoiding the overhead of such processing at the network interface level. Time-sensitive applications often use UDP because dropping packets is preferable to waiting for delayed packets, which may not be an option in a real-time system

([Wikipedia's page](https://en.wikipedia.org/wiki/User_Datagram_Protocol), emphasis our own)

This means it is highly plausible that we get faulty data packets. These are caught by the library and surfaced to the api consumer as an `InvalidNtpServerResponseException`. See this [portion of the code](https://github.com/instacart/truetime-android/blob/master/library/src/main/java/com/instacart/library/truetime/SntpClient.java#L137) for the various checks that we guard against.

These guards are *extremely* important to guarantee accurate time and cannot be avoided.


### How do I handle or protect against this in my application?

It's pretty simple:

* keep retrying the request, until you get a successfull one. Yes it does happen eventually :)
* Try picking a better NTP pool server. In our experience `time.apple.com` has worked best

Or if you want the library to just handle that, use the Rx-ified version of the library (note the -rx suffix):

```
    compile 'com.github.instacart.truetime-android:library-extension-rx:<release-version>'
```

With TrueTimeRx, we go the whole nine yards and implement the complete NTP Spec (we resolve the DNS for the provided NTP host to single IP addresses, shoot multiple requests to that single IP, guard against the above mentioned checks, retry every single failed request, filter the best response and persist that to disk). If you don't use TrueTimeRx, you don't get these benefits.

We welcome PRs for folks who wish to replicate the functionality in the vanilla TrueTime version. _We don't have plans of re-implementing that functionality atm_ in the vanilla/simple version of TrueTime.

Do also note, if TrueTime fails to initialize (because of the above exception being thrown), then an `IllegalStateException` is thrown if you try to request an actual date via `TrueTime.now()`.

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

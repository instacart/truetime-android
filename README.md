# TrueTime for Android

Get the date and time "now" impervious to manual changes to your device clock.

In certain applications it becomes important to get the real or "true" date and time. On mobile devices, if the the user has changed their clock settings manually, then a `new Date()` gives you a `Date` object impacted by the user's settings changes.

Users may do this for a variety of reasons like different timezones, trying to be punctual and setting their clocks 5/10 minutes early etc. Your application or service may want a `Date` object that is unaffected by these changes and is reliable. TrueTime gives you that.

# How is TrueTime calculated?

It's pretty simple actually, you make a network (NTP) request to a server that tells you the actual time. You then establish the "delta" between the device "uptime" and the response from the network request. Everytime "now" is requested subsequently, we account for that offset and return a corrected `Date` object.

You can read more of the juicy details in this [blog post]().

# Installation

We use [Jitpack](https://jitpack.io) to host the library.

Add this to your application's `build.gradle` file:

```
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

# Vanilla version

Importing `'com.github.instacart.truetime-android:library:<release-version>'` should be sufficient for this.

```
  TrueTime trueTime = TrueTime.get();
  trueTime.initClient();
```

`initClient` must be run on a background thread. If you run it on the main thread, you will get a [`NetworkOnMainThreadException`](https://developer.android.com/reference/android/os/NetworkOnMainThreadException.html)

You can then use:

```
  Date noReallyThisIsTheTrueDateAndTime = trueTime.now();
```

#winning...

Note: while the `IOException` is caught, we initialize an internal boolean flag that tells us if the call was a success. If the UDP call didn't go through, when you call `trueTime.now()` it will throw an `IllegalStateException`.

## Rx-ified Version

If you're down to using [RxJava](https://github.com/ReactiveX/RxJava) then there's a niftier `initClient()` api that takes in the pool of NTP hosts you want to query.

```
  List<String> ntpHosts = Arrays.asList("0.north-america.pool.ntp.org",
                                        "1.north-america.pool.ntp.org",
                                        "2.north-america.pool.ntp.org",
                                        "3.north-america.pool.ntp.org",
                                        "0.us.pool.ntp.org",
                                        "1.us.pool.ntp.org");
  trueTime.initClient(ntpHosts)
            .subscribeOn(Schedulers.io())
            .subscribe(new Action1<Void>() {
                @Override
                public void call(Void dummy) {
                    ILLog.v(TAG, "TrueTime was initialized and we have a time");
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    ILLog.logException(throwable);
                }
            });
```

Now, as before:

```
  trueTime.now(); // return a Date object with the "true" time.
```

### What is nifty about the Rx version?

* it can take in multiple NTP hosts to shoot out the UDP request
* those UDP requests are executed in parallel
* if one of the NTP requests fail, we retry constantly (the failed host alone)
* as soon as we hear back from any of the hosts, we immediately take that and terminate the rest of the requests


## Notes/tips:

* You want to `initClient` only once per device restart. This means as long as your device is not restarted, TrueTime needs to be initialized only once.
* Preferable use dependency injection (like [Dagger](http://square.github.io/dagger/)) and create a TrueTime @Singleton object
* TrueTime was built to be accurate "enough". If you want to get sub-millisecond accuracy the NTP second requires a slightly [modified synchronization algorithm](https://en.wikipedia.org/wiki/Network_Time_Protocol#Clock_synchronization_algorithm). TrueTime provides the building blocks for this. We welcome PRs if you think you can do this with TrueTimeRx pretty easily :).

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

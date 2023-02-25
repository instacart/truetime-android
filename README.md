# TrueTime for Android

![TrueTime](truetime.png "TrueTime for Android")

*⚠️ See work in progress section below. TrueTime is undergoing some changes*

----------------------------------------

*Make sure to check out our counterpart too: [TrueTime](https://github.com/instacart/TrueTime.swift), an NTP library for Swift.*

NTP client for Android. Calculate the date and time "now" impervious to manual changes to device clock time.

In certain applications it becomes important to get the real or "true" date and time. On most devices, if the clock has been changed manually, then a `Date()` instance gives you a time impacted by local settings.

Users may do this for a variety of reasons, like being in different timezones, trying to be punctual by setting their clocks 5 – 10 minutes early, etc. Your application or service may want a date that is unaffected by these changes and reliable as a source of truth. TrueTime gives you that.

You can read more about the use case in our [blog post](https://tech.instacart.com/offline-first-introducing-truetime-for-swift-and-android-15e5d968df96).

In a [conference talk](https://vimeo.com/190922794), we explained how the full NTP implementation works (with Rx). Check the [video](https://vimeo.com/190922794#t=1466s) and [slides](https://speakerdeck.com/kaushikgopal/learning-rx-by-example-2?slide=31) out for implementation details.


# Work in Progress 4.0

With the move to Kotlin & Coroutines TrueTime 4 was a [major overhaul](https://github.com/instacart/truetime-android/pull/129). We still haven't ported some of the additional bells & whistles. This section keeps track of those features (that will come in the near future). TrueTime is completely functional without these additional features, so feel free to start using it.

Most of these todos should have corresponding "TODO" comments within the code.

- [ ] Fix Jitpack import

I may have busted the jitpack import. Got to fix this first.

- [ ] Introduce a Cache provider

* Add an `interface CacheProvider` so folks can inject in their preferred caching mechanisms
* Provide a default cache implementation (probably using the non-android version of [DataStore](https://developer.android.com/topic/libraries/architecture/datastore#kts))
* ? Provide example of using this with a Database like Realm

- [ ] Algorithmic improvements

There are some exciting improvements that we have planned and use internally. Will have to upstream these changes (with a cleaner api + implementation)

- [ ] Move android dependency to separate package

There's no reason for TrueTime (with the move to coroutines) to be an "android" library. It can be a pure kotlin lib.

The only remaining dependency is `SystemClock` (which we should just have a provider for).

- [ ] Utilize all ntp pool addresses from `TrueTimeParameters.ntpHostPool`

We currently only take the first ntp host pool address from the supplied parameters. In the future, it would be nice to provide multiple ntp "pool" addresses like `time.google.com`, `time.apple.com` and utilize all of those to get the "best" value.

- [ ] BootCompletedBroadcastReceiver sample

Everytime a device is rebooted, the Truetime info is invalid. Previous libraries included an actual `BroadcastReceiver` but this is better handled by the application than the library. For safe measure, I'll include an example of how this can be done in case folks are curious.

# Installation

*⚠️ The Wiki has not been updated to the latest version of TrueTime*

[See the wiki for full instructions](https://github.com/instacart/truetime-android/wiki/How-to-use-this-library).

## Usage

```kt
val trueTime = TrueTimeImpl()
trueTime.sync()
trueTime.now()
```

💥

# Wiki has a lot of useful information

*⚠️ The Wiki has not been updated to the latest version of TrueTime*
Take a look at [the wiki sidebar](https://github.com/instacart/truetime-android/wiki) which should have a lot of useful information.

# Is this library still maintained?

The short answer is yes. We're planning a complete rewrite of TrueTime (Version 4). See [this branch](https://github.com/instacart/truetime-android/pull/129/files). We juggle our personal time between doing this rewrite and using TrueTime in its current version. TrueTime is still used in its current form at Instacart by *many* *many* users.

As is the case with most open source libraries, maintenance is hard. We've not done a great job at answering [the various issues](https://github.com/instacart/truetime-android/issues), but we want to do better. We can only ask that you trust that the rewrite is coming and it should make things better. We don't want to spend too much time making changes to the library in its existing state, because the rewrite is the better future for TrueTime.

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

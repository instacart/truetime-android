# TrueTime for Android

![TrueTime](truetime.png "TrueTime for Android")

*Make sure to check out our counterpart too: [TrueTime](https://github.com/instacart/TrueTime.swift), an NTP library for Swift.*

NTP client for Android. Calculate the date and time "now" impervious to manual changes to device clock time.

In certain applications it becomes important to get the real or "true" date and time. On most devices, if the clock has been changed manually, then a `new Date()` instance gives you a time impacted by local settings.

Users may do this for a variety of reasons, like being in different timezones, trying to be punctual by setting their clocks 5 – 10 minutes early, etc. Your application or service may want a date that is unaffected by these changes and reliable as a source of truth. TrueTime gives you that.

You can read more about the use case in our [blog post](https://tech.instacart.com/truetime/).

In a [recent conference talk](https://vimeo.com/190922794), we explained how the full NTP implementation works with Rx. Check the [video](https://vimeo.com/190922794) and [slides](https://speakerdeck.com/kaushikgopal/learning-rx-by-example-2?slide=31) out for implementation details.

Also, once we have this information it's valid until the next time you boot your device. This means if you enable the disk caching feature, after a single successful NTP request you can use the information on disk directly without ever making another network request. This applies even across application kills which can happen frequently if your users have a memory starved device.

# Installation

[See the wiki instructions](https://github.com/instacart/truetime-android/wiki/How-to-use-this-library).

# Wiki has a lot of useful information

Take a look at [the wiki sidebar](https://github.com/instacart/truetime-android/wiki) which should have a lot of useful information.

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

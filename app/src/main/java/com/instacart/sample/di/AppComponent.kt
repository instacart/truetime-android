package com.instacart.sample.di

import android.content.Context
import com.instacart.sample.App
import com.instacart.sample.TrueTimeLogEventListener
import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import com.instacart.truetime.time.TrueTimeParameters
import kotlin.time.Duration.Companion.milliseconds
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class AppComponent(
    @get:Provides val app: App,
) {
  abstract val trueTime: TrueTime

  @AppScope
  @Provides
  protected fun trueTime(listener: TrueTimeLogEventListener): TrueTime {
    val params =
        TrueTimeParameters.Builder()
            .ntpHostPool(arrayListOf("time.apple.com"))
            .connectionTimeout(31428.milliseconds)
            .syncInterval(5_000.milliseconds)
            .retryCountAgainstSingleIp(3)
            .returnSafelyWhenUninitialized(false)
            .serverResponseDelayMax(
                900.milliseconds) // this value is pretty high (coding on a plane)
            .buildParams()

    return TrueTimeImpl(params, listener = listener)
  }

  companion object {
    fun from(context: Context): AppComponent = (context.applicationContext as App).component
  }
}

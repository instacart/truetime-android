package com.instacart.sample

import android.app.Application
import com.instacart.sample.di.AppComponent

@Suppress("unused")
class App : Application() {
  private val appComponent by lazy(LazyThreadSafetyMode.NONE) { AppComponent.from(this) }

  override fun onCreate() {
    super.onCreate()

    appComponent.trueTime.sync()
  }
}

package com.instacart.sample

import android.app.Application
import com.instacart.sample.di.AppComponent
import com.instacart.sample.di.create

@Suppress("unused")
class App : Application() {
  val component by lazy(LazyThreadSafetyMode.NONE) { AppComponent::class.create(this) }

  override fun onCreate() {
    super.onCreate()

    component.trueTime.sync()
  }
}

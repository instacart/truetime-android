package com.instacart.sample

import android.app.Application

@Suppress("unused")
class App : Application() {

  //    val trueTime = TrueTimeImpl()

  override fun onCreate() {
    super.onCreate()
    //        trueTime.sync()
  }
}

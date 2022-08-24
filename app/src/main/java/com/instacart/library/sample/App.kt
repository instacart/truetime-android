package com.instacart.library.sample

import android.app.Application
import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("unused")
class App : Application() {

//    val trueTime = TrueTimeImpl()

    override fun onCreate() {
        super.onCreate()
//        trueTime.sync()
    }

}

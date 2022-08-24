package com.instacart.library.sample

import android.app.Application
import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("unused")
class App : Application() {

    companion object {
        private val TAG = App::class.java.simpleName
//        val trueTime2: TrueTime2 = TrueTimeImpl(SntpImpl())
    }

    override fun onCreate() {
        super.onCreate()

//        trueTime2.initialize(with = TrueTimeParameters())
//        initRxTrueTime()
//        initTrueTime()
    }
//
//    /**
//     * init the TrueTime using a AsyncTask.
//     */
//    private fun initTrueTime() {
//        InitTrueTimeAsyncTask().execute()
//    }
//
//    // a little part of me died, having to use this
//    private inner class InitTrueTimeAsyncTask : AsyncTask<Void, Void, Void>() {
//
//        override fun doInBackground(vararg params: Void): Void? {
//            try {
//                TrueTime.build()
//                    //.withSharedPreferences(SampleActivity.this)
//                    .withNtpHost("time.google.com")
//                    .withLoggingEnabled(false)
//                    .withSharedPreferencesCache(this@App)
//                    .withConnectionTimeout(31428)
//                    .initialize()
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e)
//            }
//
//            return null
//        }
//    }
//
//    /**
//     * Initialize the TrueTime using RxJava.
//     */
//    @SuppressLint("CheckResult")
//    private fun initRxTrueTime() {
//        TrueTimeRx.build()
//            .withConnectionTimeout(31428)
//            .withRetryCount(100)
//            .withSharedPreferencesCache(this)
//            .withLoggingEnabled(true)
//            .initializeRx("time.google.com")
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ date ->
//                Log.d(TAG, "Success initialized TrueTime :$date")
//            }, {
//                Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", it)
//            })
//    }
}

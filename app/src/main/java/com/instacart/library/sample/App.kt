package com.instacart.library.sample

import android.annotation.SuppressLint
import android.app.Application
import android.os.AsyncTask
import android.util.Log
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.TrueTime2
import com.instacart.library.truetime.TrueTimeImpl
import com.instacart.library.truetime.TrueTimeParameters
import com.instacart.library.truetime.TrueTimeRx
import com.instacart.library.truetime.sntp.SntpClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

@Suppress("unused")
class App : Application() {

    companion object {
        private val TAG = App::class.java.simpleName
        val trueTime2: TrueTime2 = TrueTimeImpl(SntpClient())
    }

    override fun onCreate() {
        super.onCreate()

        trueTime2.initialize(with = TrueTimeParameters())
        initRxTrueTime()
//        initTrueTime()
    }

    /**
     * init the TrueTime using a AsyncTask.
     */
    private fun initTrueTime() {
        InitTrueTimeAsyncTask().execute()
    }

    // a little part of me died, having to use this
    private inner class InitTrueTimeAsyncTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            try {
                TrueTime.build()
                    //.withSharedPreferences(SampleActivity.this)
                    .withNtpHost("time.google.com")
                    .withLoggingEnabled(false)
                    .withSharedPreferencesCache(this@App)
                    .withConnectionTimeout(31428)
                    .initialize()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e)
            }

            return null
        }
    }

    /**
     * Initialize the TrueTime using RxJava.
     */
    @SuppressLint("CheckResult")
    private fun initRxTrueTime() {
        TrueTimeRx.build()
            .withConnectionTimeout(31428)
            .withRetryCount(100)
            .withSharedPreferencesCache(this)
            .withLoggingEnabled(true)
            .initializeRx("time.google.com")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ date ->
                Log.d(TAG, "Success initialized TrueTime :$date")
            }, {
                Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", it)
            })
    }
}

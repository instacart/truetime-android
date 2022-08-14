package com.instacart.library.sample

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.instacart.library.sample.databinding.ActivitySampleBinding
import com.instacart.truetime.legacy.TrueTimeRx
import com.instacart.truetime.log.Logger
import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import com.instacart.truetime.time.TrueTimeParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding
    private val disposables = CompositeDisposable()

    private lateinit var trueTime: TrueTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "True Time Demo"

        binding.btnRefresh.setOnClickListener { refreshTime() }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun refreshTime() {
        binding.deviceTime.text = "Device Time: (loading...)"

        kickOffTruetimeCoroutines()
        kickOffTrueTimeRx()

        binding.deviceTime.text = "Device Time: ${formatDate(Date())}"
    }

    private fun kickOffTruetimeCoroutines() {
        binding.truetimeNew.text = "TrueTime (Coroutines): (loading...)"

        val mainDispatcherScope = CoroutineScope(Dispatchers.Main.immediate)

        if (!::trueTime.isInitialized) {
            trueTime = TrueTimeImpl(logger = AndroidLogger)
        }

        val with = TrueTimeParameters(
            connectionTimeoutInMillis = 31428,
            retryCountAgainstSingleIp = 3,
            ntpHostPool = "pool.ntp.org",
            syncIntervalInMillis = 1_000
        )

        mainDispatcherScope.launch {
            trueTime.sync(with)
        }

        binding.truetimeNew.text = "TrueTime (Coroutines): ${formatDate(trueTime.nowSafely())}"

//        Timer("Kill Sync Job", false).schedule(12_000) {
//            job.cancel()
//        }
    }

    private fun kickOffTrueTimeRx() {
        binding.truetimeLegacy.text = "TrueTime (Rx) : (loading...)"

        val d = TrueTimeRx()
            .withConnectionTimeout(31428)
            .withRetryCount(100)
//            .withSharedPreferencesCache(this)
            .withLoggingEnabled(false)
            .initializeRx("pool.ntp.org")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ date ->
                binding.truetimeLegacy.text = "TrueTime (Rx) : ${formatDate(date)}"
            }, {
                Log.e("Demo", "something went wrong when trying to initializeRx TrueTime", it)
            })

        disposables.add(d)
    }

    private fun formatDate(date: Date): String {
        return Instant
            .ofEpochMilli(date.time)
            .atZone(ZoneId.of("America/Los_Angeles"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    object AndroidLogger : Logger {
        override fun v(tag: String, msg: String) {
            Log.v(tag, msg)
        }

        override fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        override fun i(tag: String, msg: String) {
            Log.i(tag, msg)
        }

        override fun w(tag: String, msg: String) {
            Log.w(tag, msg)
        }

        override fun e(tag: String, msg: String, t: Throwable?) {
            Log.e(tag, "$msg: ${t?.message}", t)
        }
    }
}

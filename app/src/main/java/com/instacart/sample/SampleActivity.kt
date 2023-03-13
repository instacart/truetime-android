package com.instacart.sample

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.instacart.library.truetime.TrueTimeRx
import com.instacart.sample.databinding.ActivitySampleBinding
import com.instacart.truetime.time.TrueTime
import com.instacart.truetime.time.TrueTimeImpl
import com.instacart.truetime.time.TrueTimeParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
class SampleActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySampleBinding
  private val disposables = CompositeDisposable()

  private lateinit var sampleTrueTime: TrueTime
  private lateinit var job: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySampleBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.title = "True Time Demo"
    //        (application as App).trueTime.now()

    binding.btnRefresh.setOnClickListener { refreshTime() }
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
    job.cancel()
  }

  private fun refreshTime() {
    binding.deviceTime.text = "Device Time: (loading...)"

    kickOffTruetimeCoroutines()
    kickOffTrueTimeRx()

    binding.deviceTime.text = "Device Time: ${formatInstant(Instant.now())}"
  }

  private fun kickOffTruetimeCoroutines() {

    binding.truetimeNew.text = "(Coroutines): (loading...)"

    if (::job.isInitialized) job.cancel()

    if (!::sampleTrueTime.isInitialized) {
      val params =
          TrueTimeParameters.Builder()
              .ntpHostPool(arrayListOf("time.apple.com"))
              .connectionTimeoutInMillis(31428)
              .syncIntervalInMillis(5_000)
              .retryCountAgainstSingleIp(3)
              .returnSafelyWhenUninitialized(false)
              .serverResponseDelayMaxInMillis(900) // this value is pretty high (coding on a plane)
              .buildParams()

      sampleTrueTime = TrueTimeImpl(params, listener = TrueTimeLogEventListener())
    }

    job = sampleTrueTime.sync()

    lifecycleScope.launch {
      while (!sampleTrueTime.hasTheTime()) {
        delay(500)
      }

      binding.truetimeNew.text = "(Coroutines): ${formatInstant(sampleTrueTime.now())}"
    }

    if (false) {
      Timer("Kill Sync Job", false).schedule(12_000) { job.cancel() }
    }
  }

  private fun kickOffTrueTimeRx() {
    binding.truetimeLegacy.text = "(Rx) : (loading...)"

    val d =
        TrueTimeRx()
            .withConnectionTimeout(31428)
            .withRetryCount(100)
            //            .withSharedPreferencesCache(this)
            .withLoggingEnabled(false)
            .initializeRx("time.google.com")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { date -> binding.truetimeLegacy.text = "(Rx) : ${formatInstant(date.toInstant())}" },
                { Log.e("Demo", "something went wrong when trying to initializeRx TrueTime", it) },
            )

    disposables.add(d)
  }

  private fun formatInstant(instant: Instant): String {
    return instant
        .atZone(ZoneId.of("America/Los_Angeles"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  }
}

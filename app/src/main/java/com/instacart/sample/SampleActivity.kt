package com.instacart.sample

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.instacart.sample.databinding.ActivitySampleBinding
import com.instacart.sample.di.AppComponent
import com.instacart.truetime.time.TrueTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
class SampleActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySampleBinding

  private lateinit var appTrueTime: TrueTime

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appComponent = AppComponent.from(this)
    appTrueTime = appComponent.trueTime

    binding = ActivitySampleBinding.inflate(layoutInflater)
    setContentView(binding.root)
    supportActionBar?.title = "True Time Demo"
    binding.btnRefresh.setOnClickListener { refreshTime() }
  }

  private fun refreshTime() {
    binding.truetimeNew.text = "(Coroutines): (loading...)"
    checkTrueTimeHasTheTime()

    binding.deviceTime.text = "Device Time: (loading...)"
    binding.deviceTime.text = "Device Time: ${formatDate(Date())}"
  }

  private fun checkTrueTimeHasTheTime() {
    lifecycleScope.launch {
      while (!appTrueTime.hasTheTime()) {
        delay(1.seconds)
      }
      binding.truetimeNew.text = "(Coroutines): ${formatDate(appTrueTime.now())}"
    }
  }

  private fun formatDate(date: Date): String {
    return Instant.ofEpochMilli(date.time)
        .atZone(ZoneId.of("America/Los_Angeles"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  }
}

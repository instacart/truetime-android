package com.instacart.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.instacart.sample.di.AppComponent
import com.instacart.truetime.time.TrueTime
import java.util.*
import kotlinx.coroutines.*

class SampleActivity : ComponentActivity() {

  private lateinit var appTrueTime: TrueTime

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appComponent = AppComponent.from(this)
    appTrueTime = appComponent.trueTime
    setContent { Text("Hello World") }

    //    binding.btnRefresh.setOnClickListener { refreshTime() }
  }

  //  private fun refreshTime() {
  //    binding.truetimeNew.text = "(Coroutines): (loading...)"
  //    checkTrueTimeHasTheTime()
  //
  //    binding.deviceTime.text = "Device Time: (loading...)"
  //    binding.deviceTime.text = "Device Time: ${formatDate(Date())}"
  //  }
  //
  //  private fun checkTrueTimeHasTheTime() {
  //    lifecycleScope.launch {
  //      while (!appTrueTime.hasTheTime()) {
  //        delay(1.seconds)
  //      }
  //      binding.truetimeNew.text = "(Coroutines): ${formatDate(appTrueTime.now())}"
  //    }
  //  }
  //
  //  private fun formatDate(date: Date): String {
  //    return Instant.ofEpochMilli(date.time)
  //        .atZone(ZoneId.of("America/Los_Angeles"))
  //        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  //  }
}

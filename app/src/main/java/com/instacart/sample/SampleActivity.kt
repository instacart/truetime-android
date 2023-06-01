package com.instacart.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.instacart.sample.di.AppComponent
import com.instacart.truetime.time.TrueTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SampleActivity : ComponentActivity() {

  private lateinit var appTrueTime: TrueTime

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appComponent = AppComponent.from(this)
    appTrueTime = appComponent.trueTime

    setContent { TimeScreen() }
  }

  @Composable
  private fun TimeScreen() {
    var state by remember { mutableStateOf(TimeInfoState()) }

    TimeInfo(
        state,
        hoistBtnClick = {
          state = state.copy(deviceTime = "Device: ${formatDate(Date())}")
          lifecycleScope.launch { state = state.copy(trueTime = getFormattedTrueTime()) }
        })
  }

  @Composable
  fun TimeInfo(state: TimeInfoState, hoistBtnClick: () -> Unit = {}) {

    Column(
        modifier = Modifier.padding(20.dp).fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(text = state.deviceTime, style = MaterialTheme.typography.headlineSmall)

      Text(text = state.trueTime, style = MaterialTheme.typography.headlineSmall)

      Button(onClick = hoistBtnClick, modifier = Modifier.padding(20.dp)) {
        Text(text = "Update Time", fontSize = 25.sp)
      }
    }
  }

  private suspend fun getFormattedTrueTime(): String {
    while (!appTrueTime.hasTheTime()) {
      delay(1.seconds)
    }
    return "TrueTime: ${formatDate(appTrueTime.now())}"
  }

  private fun formatDate(date: Date): String {
    return Instant.ofEpochMilli(date.time)
        .atZone(ZoneId.of("America/Los_Angeles"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  }

  //  @Preview
  //  @Composable
  //  fun PreviewTimeInfo() {
  //    TimeInfo()
  //  }

  data class TimeInfoState(
      val deviceTime: String = "Device Time: (loading ...)",
      val trueTime: String = "True Time: (loading ...)",
  )
}

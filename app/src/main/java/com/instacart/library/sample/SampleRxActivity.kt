package com.instacart.library.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.android.synthetic.main.activity_sample.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SampleRxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        supportActionBar?.title = "TrueTimeRx"

        refreshBtn?.isEnabled = TrueTimeRx.isInitialized()
        refreshBtn.setOnClickListener {
            updateTime()
        }
        nonRxBtn.setOnClickListener {
            startActivity(Intent(this, SampleActivity::class.java))
        }
    }

    private fun updateTime() {
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        refreshBtn.isEnabled = true
        val trueTime = TrueTimeRx.now()
        val deviceTime = Date()

        Log.d(
            "kg",
            String.format(
                " [trueTime: %d] [devicetime: %d] [drift_sec: %f]",
                trueTime.time,
                deviceTime.time,
                (trueTime.time - deviceTime.time) / 1000f
            )
        )

        timeGMT.text = getString(
            R.string.tt_time_gmt,
            formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"))
        )
        timePST.text = getString(
            R.string.tt_time_pst,
            formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))
        )
        timeDeviceTime.text = getString(
            R.string.tt_time_device,
            formatDate(
                deviceTime,
                "yyyy-MM-dd HH:mm:ss",
                TimeZone.getTimeZone("GMT-07:00")
            )
        )
    }

    private fun formatDate(date: Date, pattern: String, timeZone: TimeZone): String {
        val format = SimpleDateFormat(pattern, Locale.ENGLISH)
        format.timeZone = timeZone
        return format.format(date)
    }
}

package com.instacart.library.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instacart.library.truetime.time.TrueTime
import kotlinx.android.synthetic.main.activity_sample.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SampleActivity : AppCompatActivity() {

    private val tt2 = App.trueTime2 // dirty DI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        supportActionBar?.title = "TrueTime"

        refreshBtn.isEnabled = TrueTime.isInitialized()
        refreshBtn.setOnClickListener {
            updateTime()
        }
        nonRxBtn.visibility = View.GONE
    }

    private fun updateTime() {
        if (!TrueTime.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized. Trying again.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        testing()

        val trueTime = TrueTime.now()
        val deviceTime = Date()

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
            formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))
        )
    }

    private fun testing() {
        Toast.makeText(this, "tt2 ${tt2.nowSafely()}", Toast.LENGTH_SHORT)
            .show()
    }

    private fun formatDate(date: Date, pattern: String, timeZone: TimeZone): String {
        val format = SimpleDateFormat(pattern, Locale.ENGLISH)
        format.timeZone = timeZone
        return format.format(date)
    }
}

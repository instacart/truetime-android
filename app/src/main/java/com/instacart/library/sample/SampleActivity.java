package com.instacart.library.sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.instacart.library.truetime.TrueTime;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SampleActivity
      extends AppCompatActivity {

    private static final String TAG = SampleActivity.class.getSimpleName();

    @Bind(R.id.tt_time_gmt) TextView timeGMT;
    @Bind(R.id.tt_time_pst) TextView timePST;
    @Bind(R.id.tt_time_device) TextView timeDeviceTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        new InitTrueTimeAsyncTask().execute();

        ButterKnife.bind(this);
    }

    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        if (!TrueTime.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized. Trying again.", Toast.LENGTH_SHORT).show();
            new InitTrueTimeAsyncTask().execute();
            return;
        }

        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();

        timeGMT.setText(getString(R.string.tt_time_gmt,
                                  _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"))));
        timePST.setText(getString(R.string.tt_time_pst,
                                  _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))));
        timeDeviceTime.setText(getString(R.string.tt_time_device,
                                         _formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))));
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    // a little part of me died, having to use this
    private class InitTrueTimeAsyncTask
          extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                      //.withSharedPreferences(SampleActivity.this)
                      .withNtpHost("0.north-america.pool.ntp.org")
                      .withLoggingEnabled(false)
                      .withConnectionTimeout(3_1428)
                      .initialize();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception when trying to get TrueTime", e);
            }
            return null;
        }
    }

}

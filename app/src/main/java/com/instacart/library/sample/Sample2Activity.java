package com.instacart.library.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTimeRx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Sample2Activity
      extends AppCompatActivity {

    private static final String TAG = Sample2Activity.class.getSimpleName();

    @Bind(R.id.tt_btn_refresh) Button refreshBtn;
    @Bind(R.id.tt_time_gmt) TextView timeGMT;
    @Bind(R.id.tt_time_pst) TextView timePST;
    @Bind(R.id.tt_time_device) TextView timeDeviceTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        getSupportActionBar().setTitle("TrueTimeRx");

        ButterKnife.bind(this);
        refreshBtn.setEnabled(false);

        //TrueTimeRx.clearCachedInfo(this);

        TrueTimeRx.build()
              .withConnectionTimeout(31_428)
              .withRetryCount(100)
              .withSharedPreferencesCache(getApplicationContext())
              .withLoggingEnabled(true)
              .initializeRx("time.google.com")
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Consumer<Date>() {
                  @Override
                  public void accept(Date date) {
                      onBtnRefresh();
                  }
              }, new Consumer<Throwable>() {
                  @Override
                  public void accept(Throwable throwable) {
                      updateTime();
                      Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", throwable);
                  }
              }, new Action() {
                  @Override
                  public void run() {
                      refreshBtn.setEnabled(true);
                  }
              });
    }

    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        updateTime();
    }

    private void updateTime() {
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show();
            return;
        }
        refreshBtn.setEnabled(true);
        Date trueTime = TrueTimeRx.now();
        Date deviceTime = new Date();

        Log.d("kg",
              String.format(" [trueTime: %d] [devicetime: %d] [drift_sec: %f]",
                            trueTime.getTime(),
                            deviceTime.getTime(),
                            (trueTime.getTime() - deviceTime.getTime()) / 1000F));

        timeGMT.setText(getString(R.string.tt_time_gmt,
                                  _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"))));
        timePST.setText(getString(R.string.tt_time_pst,
                                  _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))));
        timeDeviceTime.setText(getString(R.string.tt_time_device,
                                         _formatDate(deviceTime,
                                                     "yyyy-MM-dd HH:mm:ss",
                                                     TimeZone.getTimeZone("GMT-07:00"))));
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }
}

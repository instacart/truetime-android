package com.instacart.library.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.extensionrx.TrueTimeRx;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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

        ButterKnife.bind(this);
        refreshBtn.setEnabled(false);

        List<String> ntpHosts = Arrays.asList("time.apple.com",
                                              "0.north-america.pool.ntp.org",
                                              "1.north-america.pool.ntp.org",
                                              "2.north-america.pool.ntp.org",
                                              "3.north-america.pool.ntp.org",
                                              "0.us.pool.ntp.org",
                                              "1.us.pool.ntp.org");
        //TrueTimeRx.clearCachedInfo(prefs);

        TrueTimeRx.build()
              .withConnectionTimeout(31_428)
              .withRetryCount(100)
              .withSharedPreferences(this)
              .initialize(ntpHosts)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Action1<Date>() {
                  @Override
                  public void call(Date date) {
                      onBtnRefresh();
                  }
              }, new Action1<Throwable>() {
                  @Override
                  public void call(Throwable throwable) {
                      Log.e(TAG, "something went wrong when trying to initialize TrueTime", throwable);
                  }
              }, new Action0() {
                  @Override
                  public void call() {
                      refreshBtn.setEnabled(true);
                  }
              });
    }

    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        Date trueTime = TrueTime.now();
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
                                         _formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))));
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }
}

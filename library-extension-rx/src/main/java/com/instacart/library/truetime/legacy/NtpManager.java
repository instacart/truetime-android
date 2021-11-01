package com.instacart.library.truetime.legacy;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import io.reactivex.schedulers.Schedulers;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NtpManager {

//  private static final String TAG = "QED:NtpManager";
//  private final AppSettings appSettings;
//  private SharedPreferences.OnSharedPreferenceChangeListener settingChangeListener;
//
//  private static final long NTP_INIT_FAILED_RETRY_INTERVAL_MINUTES = 1;
//  private static final long CHECK_FOR_TIME_CHANGE_MILLIS = 1000;
//
//  private static final int MIN_POINTS_TIME_FIT = 2;
//  private static final int MAX_POINTS_TIME_FIT = 3;
//  private static final long REBUILD_LINEAR_FIT_THRESHOLD_DISCREPANCY_MILLIS = 500;
//
//  private static int mNumTrueTimeCalls = 0;
//  private static long mTrueTimeInitRequestMillis = 0;
//  private static boolean mTrueTimeInitialized = false;
//  private static boolean mTrueTimeSyncInProgress = false;
//  private static long mTrueTimeLastSyncMillis = 0;
//  private static long mElapsedRealtimeWhenNextSyncWasScheduled = 0;
//  private static String mNtpPoolServerInUse;
//
//  private static long mCurResyncMinutes = 1;
//  private static int mCurNtpResyncIntervalHours;
//  private static RunningStats mClockDriftSampleTimes = new RunningStats(MAX_POINTS_TIME_FIT,
//      EnumSet.of(RunningStats.RunningStatsOptions.FIXEDSIZE));
//  private static RunningStats mClockDriftSampleOffsets = new RunningStats(MAX_POINTS_TIME_FIT,
//      EnumSet.of(RunningStats.RunningStatsOptions.FIXEDSIZE));
//  private static long mClockDriftReferenceMillis;
//  private static double mClockDriftSlope = 0;
//  private static double mClockDriftIntercept = 0;
//  private static long mPrevDeltaCurMinusElapsedMillis = 0;
//  private static long mNtpResyncThresholdTimeChangeMillis = TimeUnit.DAYS
//      .toMillis(1);    // some big number to effectively disable by default
//
//  private final HandlerThread mNtpManagerThread = new HandlerThread("updateTrueTime");
//  private Handler mUpdateTrueTimeHandler;
//  private Handler mCheckForTimeChangeHandler;
//
//  public NtpManager() {
//    Thread.setDefaultUncaughtExceptionHandler(App.uncaughtExceptionHandler);
//    appSettings = App.getAppSettings();
//    App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid()) + "starting NtpManager thread and handlers");
//
//    // initialize TrueTime updating
//    mNtpManagerThread.start();
//    mUpdateTrueTimeHandler = new Handler(mNtpManagerThread.getLooper());
//
//    // initialize time-change tracking
//    mNtpResyncThresholdTimeChangeMillis = appSettings.getNtpResyncThresholdTimeChangeMillis();
//    mPrevDeltaCurMinusElapsedMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
//    App.debugStream(TAG,
//        App.threadIdWithDelim(android.os.Process.myTid()) + "time-change tracking initialized: threshold = "
//            + mNtpResyncThresholdTimeChangeMillis + " milliseconds");
//    mCheckForTimeChangeHandler = new Handler(mNtpManagerThread.getLooper());
//    mCheckForTimeChangeHandler.post(checkForTimeChange);
//
//    createAppSettingChangeListener();
//
//    // don't call initialize() here; App.onCreate() calls after setting up some other NTP- & timing-related stuff
//  }
//
//  public void initialize() {
//    mUpdateTrueTimeHandler.removeCallbacksAndMessages(null);    // in case this is a re-initialization!
//    mTrueTimeInitialized = false;
//    mNumTrueTimeCalls = 0;
//    mCurResyncMinutes = 1;
//    mCurNtpResyncIntervalHours = appSettings.getNtpResyncIntervalHours();
//    mClockDriftSampleTimes.clear();
//    mClockDriftSampleOffsets.clear();
//    App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid()) + "initializing TrueTimeRx",
//        AppSettings.DebugLevel.IMPORTANT);
//    scheduleNextResyncAttempt(0);
//  }
//
//  private void scheduleNextResyncAttempt(long millisFromNow) {
//    if (millisFromNow == 0) {
//      mUpdateTrueTimeHandler.post(updateTrueTimeRx);
//      App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid()) + "updateTrueTimeRx scheduled for now",
//          AppSettings.DebugLevel.IMPORTANT);
//    } else {
//      mUpdateTrueTimeHandler.postDelayed(updateTrueTimeRx, millisFromNow);
//      App.debugStream(TAG,
//          App.threadIdWithDelim(android.os.Process.myTid()) + "updateTrueTimeRx scheduled for " + millisFromNow
//              + " ms from now", AppSettings.DebugLevel.IMPORTANT);
//    }
//    mElapsedRealtimeWhenNextSyncWasScheduled = SystemClock.elapsedRealtime();
//  }
//
//  final Runnable updateTrueTimeRx = new Runnable() {
//    @Override
//    public void run() {
//      // don't start a new sync if one already/still in progress or sync is disabled
//      if (mTrueTimeSyncInProgress || !appSettings.ntpResyncIsEnabled()) {
//        return;
//      }
//      mTrueTimeSyncInProgress = true;
//
//      String dbgPfx = App.threadIdWithDelim(android.os.Process.myTid());
//
//      // try to get NTP fix
//      // on success, if appSettings.ntpResyncIsEnabled() queue up next round in appSettings.getNtpResyncIntervalHours()
//      // on failure, if appSettings.ntpResyncIsEnabled() queue up a re-try in NTP_INIT_FAILED_RETRY_INTERVAL_MINUTES
//
//      // keep track of number of attempts this round
//      ++mNumTrueTimeCalls;
//      // get current NTP pool server to use (allow server to be changed b/n attempts)
//      mNtpPoolServerInUse = appSettings.getNtpPoolServer();
//      // if 1st attempt this round, initialize request time
//      if (mNumTrueTimeCalls == 1) {
//        mTrueTimeInitRequestMillis = SystemClock.elapsedRealtime();
//      }
//      App.debugStream(TAG, dbgPfx + "attempt " + mNumTrueTimeCalls + " to init TrueTimeRx",
//          AppSettings.DebugLevel.IMPORTANT);
//      TrueTimeRx.build()
//          .withLoggingEnabled(true)
//          .withConnectionTimeout(10_000)
//          .withRetryCount(5)
//          .initializeRx(mNtpPoolServerInUse)
//          .subscribeOn(Schedulers.io())
//          .subscribe(date -> {
//            // if (still) enabled, update status/estimation and queue up next round
//            if (appSettings.ntpResyncIsEnabled()) {
//              mTrueTimeInitialized = true;
//              mTrueTimeLastSyncMillis = SystemClock.elapsedRealtime();
//              updateNtpOffsetEstimation();
//              App.debugStream(TAG,
//                  dbgPfx + "TrueTimeRx obtained after " + mNumTrueTimeCalls + " attempt(s) taking " + App
//                      .hmsSince(mTrueTimeInitRequestMillis) + "; offset = " + getNtpSysOffsetMillisString(),
//                  AppSettings.DebugLevel.IMPORTANT);
//              long resyncDelayMillis = nextResyncDelayMillis();
//              App.debugStream(TAG,
//                  dbgPfx + "Queuing up next TrueTimeRx sync in " + TimeUnit.MILLISECONDS.toMinutes(resyncDelayMillis)
//                      + " minutes");
//              mNumTrueTimeCalls = 0;  // reset so subsequent runs know this is a new sync, not a retry
//              scheduleNextResyncAttempt(resyncDelayMillis);
//            }
//            mTrueTimeSyncInProgress = false;
//          }, throwable -> {
//            App.errorStream(TAG,
//                App.threadIdWithDelim(android.os.Process.myTid()) + "TrueTimeRx error in attempt " + mNumTrueTimeCalls
//                    + ": " + throwable.getMessage());
//            // if resync is (still) enabled, queue up another attempt else terminate this one
//            if (appSettings.ntpResyncIsEnabled()) {
//              // queue up a retry for later (may be ignored if TrueTimeRx's own internal retries succeed before then)
//              App.debugStream(TAG,
//                  dbgPfx + "Queuing up another TrueTimeRx attempt in " + NTP_INIT_FAILED_RETRY_INTERVAL_MINUTES
//                      + " minutes");
//              scheduleNextResyncAttempt(TimeUnit.MINUTES.toMillis(NTP_INIT_FAILED_RETRY_INTERVAL_MINUTES));
//            }
//            mTrueTimeSyncInProgress = false;
//          });
//    }
//  };
//
//  public String getNtpStatus() {
//    if (appSettings.ntpResyncIsEnabled()) {
//      if (mTrueTimeInitialized) {
//        return getNtpSysOffsetMillisString() + " " + App.hmsSince(mTrueTimeLastSyncMillis) +
//            (mTrueTimeSyncInProgress ?
//                " updating from " + mNtpPoolServerInUse + " " +
//                    App.hmsSince(mTrueTimeInitRequestMillis) + " " +
//                    mNumTrueTimeCalls :
//                "");
//      } else if (mTrueTimeSyncInProgress) {
//        return "trying " + mNtpPoolServerInUse + " " + App.hmsSince(mTrueTimeInitRequestMillis) + " "
//            + mNumTrueTimeCalls;
//      } else {
//        App.errorStream(TAG,
//            App.threadIdWithDelim(android.os.Process.myTid()) + "NTP sync enabled but not initialized nor in-progress");
//        return "failed - retry in " + NTP_INIT_FAILED_RETRY_INTERVAL_MINUTES + " min";
//      }
//    } else {
//      return "disabled";
//    }
//  }
//
//  public static boolean getNtpTimeInUse() {
//    return mTrueTimeInitialized;
//  }
//
//  private long nextResyncDelayMillis() {
//    long maxMinutes = TimeUnit.HOURS.toMinutes(appSettings.getNtpResyncIntervalHours());
//    mCurResyncMinutes = Math.min(mCurResyncMinutes * 2, maxMinutes);
//    return TimeUnit.MINUTES.toMillis(mCurResyncMinutes);
//  }
//
//  private void updateNtpOffsetEstimation() {
//    String dbgPfx = App.threadIdWithDelim(android.os.Process.myTid());
//
//    // if this is the first NTP fix in a new linear fit, init the reference time
//    if (mClockDriftSampleTimes.size() == 0) {
//      mClockDriftReferenceMillis = mTrueTimeLastSyncMillis;
//    }
//
//    // get time-since-reference and NTP offset-from-system-time for this new NTP fix
//    long elapsedMillis = mTrueTimeLastSyncMillis - mClockDriftReferenceMillis;
//    long ntpOffsetMillis = TrueTimeRx.nowMillis() - System.currentTimeMillis();
//
//    // if already had enough points for linear fit, compare predicted w/ this new offset; start new linear fit if too different
//    if (mClockDriftSampleTimes.size() >= MIN_POINTS_TIME_FIT) {
//      long differenceFromEstimated = ntpOffsetMillis - estimatedNtpOffset();
//      if (Math.abs(differenceFromEstimated) > REBUILD_LINEAR_FIT_THRESHOLD_DISCREPANCY_MILLIS) {
//        mCurResyncMinutes = 1;                                  // restart rapid NTP sampling sequence
//        mClockDriftReferenceMillis = mTrueTimeLastSyncMillis;   // make this new sample the reference sample
//        elapsedMillis = 0;
//        mClockDriftSampleTimes.clear();
//        mClockDriftSampleOffsets.clear();
//        App.debugStream(TAG,
//            dbgPfx + "diff of TrueTimeRx offset from prediction " + differenceFromEstimated + " exceeds thresh " +
//                REBUILD_LINEAR_FIT_THRESHOLD_DISCREPANCY_MILLIS +
//                " -- re-building linear fit", AppSettings.DebugLevel.IMPORTANT);
//      }
//    }
//
//    // add the new sample
//    mClockDriftSampleTimes.add(elapsedMillis);
//    mClockDriftSampleOffsets.add(ntpOffsetMillis);
//    App.debugStream(TAG,
//        dbgPfx + "clock drift sample: " + mClockDriftSampleTimes.size() + " " + elapsedMillis + " " + ntpOffsetMillis,
//        AppSettings.DebugLevel.IMPORTANT);
//
//    // having added new sample, if have enough points for linear fit, compute fit params
//    if (mClockDriftSampleTimes.size() >= MIN_POINTS_TIME_FIT) {
//      // compute linear estimation parameters for offset as a function of elapsed time
//      double x_mean = mClockDriftSampleTimes.mean();
//      double y_mean = mClockDriftSampleOffsets.mean();
//      double sum_xy = 0.0;
//      double sum_x2 = 0.0;
//      Object x_vals[] = mClockDriftSampleTimes.toArray();
//      Object y_vals[] = mClockDriftSampleOffsets.toArray();
//      for (int i = 0; i < x_vals.length; i++) {
//        double dx = (double) x_vals[i] - x_mean;
//        double dy = (double) y_vals[i] - y_mean;
//        sum_xy += dx * dy;
//        sum_x2 += dx * dx;
//      }
//      double line_fit_m = sum_xy / sum_x2;
//      double line_fit_c = y_mean - (line_fit_m * x_mean);
//      setTimingFitVariables(line_fit_m, line_fit_c);
//      App.debugStream(TAG,
//          dbgPfx + "updated clock drift linear estimation: slope=" + line_fit_m + " intercept=" + line_fit_c
//              + " #points=" + mClockDriftSampleTimes.size(), AppSettings.DebugLevel.IMPORTANT);
//    } else {
//      // just use the latest NTP offset until we have enough points for linear estimation
//      setTimingFitVariables(0, ntpOffsetMillis);
//      App.debugStream(TAG,
//          dbgPfx + "updated clock drift to last observed offset: slope=" + 0 + " intercept=" + ntpOffsetMillis,
//          AppSettings.DebugLevel.IMPORTANT);
//    }
//  }
//
//  private synchronized void setTimingFitVariables(double slope, double intercept) {
//    mClockDriftSlope = slope;
//    mClockDriftIntercept = intercept;
//  }
//
//  private static synchronized long estimatedNtpOffset() {
//    double elapsedMillis = SystemClock.elapsedRealtime() - mClockDriftReferenceMillis;
//    return (long) (elapsedMillis * mClockDriftSlope + mClockDriftIntercept);
//  }
//
//  public static long bestAvailableTimeMillis() {
//    return System.currentTimeMillis() + (mTrueTimeInitialized ? estimatedNtpOffset() : 0);
//  }
//
//  public static String getNtpSysOffsetMillisString() {
//    return mTrueTimeInitialized ?
//        String.format(Locale.US, "%d", estimatedNtpOffset()) :
//        "n/a";
//  }
//
//  final Runnable checkForTimeChange = new Runnable() {
//    @Override
//    public void run() {
//      // continuously keep track of diff b/n system current-time and system elapsed time to detect current-time changes
//      long deltaMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
//      // if we are already NTP-sync'd, and time-change > thresh, need to re-start NTP sync process b/c NTP offset is based on system current-time
//      if (mTrueTimeInitialized) {
//        long timeChangeMillis = deltaMillis - mPrevDeltaCurMinusElapsedMillis;
//        if (Math.abs(timeChangeMillis) > mNtpResyncThresholdTimeChangeMillis) {
//          App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid()) + "time change of " + timeChangeMillis
//              + " exceeds threshold of +/-" + mNtpResyncThresholdTimeChangeMillis, AppSettings.DebugLevel.IMPORTANT);
//          initialize();
//        }
//      }
//      mPrevDeltaCurMinusElapsedMillis = deltaMillis;
//      mCheckForTimeChangeHandler.postDelayed(checkForTimeChange, CHECK_FOR_TIME_CHANGE_MILLIS);
//    }
//  };
//
//  private void createAppSettingChangeListener() {
//    settingChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//      public void onSharedPreferenceChanged(SharedPreferences settings, String key) {
//        if (key.equals(AppSettings.KEY_NTP_RESYNC_INTERVAL_HOURS)) {
//          // if new setting disables resync (by setting interval to 0), disable TrueTime as well and revert to device's system time
//          if (!appSettings.ntpResyncIsEnabled()) {
//            mUpdateTrueTimeHandler.removeCallbacksAndMessages(null);
//            App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid())
//                + "NTP synchronization now disabled -- using uncorrected system time");
//            mTrueTimeInitialized = false;
//          } else if (!mTrueTimeSyncInProgress) {
//            // if resync has been disabled until now, initialize a whole new process
//            if (mCurNtpResyncIntervalHours == 0) {
//              App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid())
//                  + "NTP synchronization now enabled -- initializing");
//              initialize();
//            }
//            // else reschedule the next sync adjusted for the new interval, if possible
//            else {
//              mUpdateTrueTimeHandler.removeCallbacksAndMessages(null);
//              mNumTrueTimeCalls = 0;
//              App.debugStream(TAG, App.threadIdWithDelim(android.os.Process.myTid())
//                  + "rescheduling next NTP sync on change of resync interval");
//              long millisSinceLastScheduled = SystemClock.elapsedRealtime() - mElapsedRealtimeWhenNextSyncWasScheduled;
//              long millisUntilNextSyncUsingNewInterval = nextResyncDelayMillis() - millisSinceLastScheduled;
//              if (millisUntilNextSyncUsingNewInterval > 0) {
//                scheduleNextResyncAttempt(millisUntilNextSyncUsingNewInterval);
//              } else {
//                scheduleNextResyncAttempt(0);
//              }
//            }
//          }
//          // else sync is in progress; new interval will be used when that sync completes
//
//          // in any/every case, update the local copy of the new interval
//          mCurNtpResyncIntervalHours = appSettings.getNtpResyncIntervalHours();
//        } else if (key.equals(AppSettings.KEY_NTP_RESYNC_THRESHOLD_TIME_CHANGE_MILLIS)) {
//          mNtpResyncThresholdTimeChangeMillis = appSettings.getNtpResyncThresholdTimeChangeMillis();
//        } else if (key.equals(AppSettings.KEY_NTP_POOL_SERVER)) {
//          // don't do this: mNtpPoolServerInUse = appSettings.getNtpPoolServer();
//          // b/c it might change the status messages w/o changing the server in use during an update attempt;
//          // instead, let the updateTrueTimeRx handler update this local value at the start of a new attempt
//        }
//      }
//    };
//    appSettings.registerChangeListener(settingChangeListener);
//  }
//
}

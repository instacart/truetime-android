package com.instacart.library.truetime.extensionrx;

import android.content.SharedPreferences;
import com.instacart.library.truetime.TrueTime;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TrueTimeRx
      extends TrueTime {

    private static final String TAG = TrueTimeRx.class.getSimpleName();
    private static final TrueTime INSTANCE = new TrueTimeRx();

    private int _retryCount = 50;

    public static TrueTimeRx build() {
        return (TrueTimeRx) INSTANCE;
    }

    public TrueTimeRx withSharedPreferences(SharedPreferences preferences) {
        super.withSharedPreferences(preferences);
        return (TrueTimeRx) INSTANCE;
    }

    public TrueTimeRx withConnectionTimeout(int timeout) {
        super.withConnectionTimeout(timeout);
        return (TrueTimeRx) INSTANCE;
    }

    public TrueTimeRx withRetryCount(int retryCount) {
        _retryCount = retryCount;
        return (TrueTimeRx) INSTANCE;
    }

    /**
     * Initialize the SntpClient
     * Issue SNTP call via UDP to list of provided hosts
     * Pick the first successful call and return
     * Retry failed calls individually
     */
    public Observable<Date> initialize(final List<String> ntpHosts) {
        return Observable//
              .from(ntpHosts)//
              .flatMap(new Func1<String, Observable<Date>>() {
                  @Override
                  public Observable<Date> call(String ntpHost) {
                      return Observable//
                            .just(ntpHost)//
                            .subscribeOn(Schedulers.io())//
                            .flatMap(new Func1<String, Observable<Date>>() {
                                @Override
                                public Observable<Date> call(String ntpHost) {
                                    try {
                                        initialize(ntpHost);
                                    } catch (IOException e) {
                                        return Observable.error(e);
                                    }
                                    return Observable.just(now());
                                }
                            })//
                            .retry(_retryCount)//
                            .onErrorReturn(new Func1<Throwable, Date>() {
                                @Override
                                public Date call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    return null;
                                }
                            })
                            .take(1)
                            .doOnNext(new Action1<Date>() {
                                @Override
                                public void call(Date date) {
                                    cacheTrueTimeInfo(sharedPreferences);
                                }
                            });
                  }
              })//
              .filter(new Func1<Date, Boolean>() {
                  @Override
                  public Boolean call(Date date) {
                      return date != null;
                  }
              })//
              .take(1);
    }
}

package com.instacart.library.truetime.extensionrx;

import android.util.Log;
import com.instacart.library.truetime.SntpClient;
import com.instacart.library.truetime.TrueTime;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TrueTimeRx
      extends TrueTime {

    private static final String TAG = TrueTimeRx.class.getSimpleName();

    private static final TrueTime INSTANCE = new TrueTimeRx();

    public static TrueTimeRx build() {
        return (TrueTimeRx) INSTANCE;
    }

    public TrueTimeRx withConnectionTimeout(int timeout) {
        super.withConnectionTimeout(timeout);
        return (TrueTimeRx) INSTANCE;
    }

    /**
     * Initialize the SntpClient
     * Issue Sntp call via UDP to list of provided hosts
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
                            .map(new Func1<String, Date>() {
                                @Override
                                public Date call(String host) {
                                    SntpClient sntpClient = new SntpClient();
                                    try {
                                        Log.i(TAG, "---- Querying host : " + host);
                                        sntpClient.requestTime(host, getUdpSocketTimeout());
                                        setSntpClient(sntpClient);

                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    return now();
                                }
                            })//
                            .retry(50)//
                            .onErrorReturn(new Func1<Throwable, Date>() {
                                @Override
                                public Date call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    return null;
                                }
                            });
                  }
              }, 3)//
              .filter(new Func1<Date, Boolean>() {
                  @Override
                  public Boolean call(Date date) {
                      return date != null;
                  }
              })//
              .first();
    }
}

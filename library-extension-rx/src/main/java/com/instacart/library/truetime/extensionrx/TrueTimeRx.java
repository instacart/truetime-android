package com.instacart.library.truetime.extensionrx;

import android.util.Log;
import com.instacart.library.truetime.SntpClient;
import com.instacart.library.truetime.TrueTime;
import java.util.List;
import java.util.Random;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TrueTimeRx
      extends TrueTime {

    private static final String TAG = TrueTimeRx.class.getSimpleName();

    private TrueTimeRx() {

    }

    public static TrueTimeRx get() {
        return new TrueTimeRx();
    }

    /**
     * initialize the SntpClient by providing a list of hosts
     *
     * Pick the first
     */
    public Observable<Void> initClient(final List<String> ntpHosts) {

        return Observable//
              .from(ntpHosts)//
              .flatMap(new Func1<String, Observable<SntpClient>>() {
                  @Override
                  public Observable<SntpClient> call(String ntpHost) {
                      return Observable//
                            .just(ntpHost)//
                            .subscribeOn(Schedulers.io())//
                            .map(new Func1<String, SntpClient>() {
                                @Override
                                public SntpClient call(String host) {
                                    SntpClient sntpClient = new SntpClient();
                                    try {
                                        Log.d(TAG, "Querying host : " + host);
                                        sntpClient.requestTime(host, getUdpSocketTimeout());
                                        setSntpClient(sntpClient);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    return sntpClient;
                                }
                            })//
                            .retry(5)//
                            .onErrorReturn(new Func1<Throwable, SntpClient>() {
                                @Override
                                public SntpClient call(Throwable throwable) {
                                    return null;
                                }
                            });
                  }
              }, 3)//
              .filter(new Func1<SntpClient, Boolean>() {
                  @Override
                  public Boolean call(SntpClient sntpClient) {
                      return sntpClient != null;
                  }
              })//
              .first()//
              .map(new Func1<SntpClient, Void>() {
                  @Override
                  public Void call(SntpClient sntpClient) {
                      return null;
                  }
              });
    }

    // hacking
    private static int randomIntBetween(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

}

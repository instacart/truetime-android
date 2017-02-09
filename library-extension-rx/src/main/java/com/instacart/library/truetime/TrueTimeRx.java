package com.instacart.library.truetime;

import android.content.Context;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TrueTimeRx
      extends TrueTime {

    private static final TrueTimeRx RX_INSTANCE = new TrueTimeRx();
    private static final String TAG = TrueTimeRx.class.getSimpleName();

    private int _retryCount = 50;

    public static TrueTimeRx build() {
        return RX_INSTANCE;
    }

    public TrueTimeRx withSharedPreferences(Context context) {
        super.withSharedPreferences(context);
        return this;
    }

    public TrueTimeRx withConnectionTimeout(int timeout) {
        super.withConnectionTimeout(timeout);
        return this;
    }

    public TrueTimeRx withLoggingEnabled(boolean isLoggingEnabled) {
        super.withLoggingEnabled(isLoggingEnabled);
        return this;
    }

    public TrueTimeRx withRetryCount(int retryCount) {
        _retryCount = retryCount;
        return this;
    }

    /**
     * Initialize TrueTime
     * See {@link #initializeNtp(String)} for details on working
     *
     * @return accurate NTP Date
     */
    public Observable<Date> initializeRx(String ntpPoolAddress) {
        return initializeNtp(ntpPoolAddress).map(new Func1<long[], Date>() {
            @Override
            public Date call(long[] longs) {
                return now();
            }
        });
    }

    /**
     * Initialize TrueTime
     * A single NTP pool server is provided.
     * Using DNS we resolve that to multiple IP hosts (See {@link #initializeNtp(List)} for manually resolved IPs)
     *
     * Use this instead of {@link #initializeRx(String)} if you wish to also get additional info for
     * instrumentation/tracking actual NTP response data
     *
     * @param ntpPool NTP pool server e.g. time.apple.com, 0.us.pool.ntp.org
     * @return Observable of detailed long[] containing most important parts of the actual NTP response
     * See RESPONSE_INDEX_ prefixes in {@link SntpClient} for details
     */
    public Observable<long[]> initializeNtp(String ntpPool) {
        return Observable
              .just(ntpPool)
              .compose(resolveNtpPoolToIpAddresses())
              .compose(performNtpAlgorithm());
    }

    /**
     * Initialize TrueTime
     * Use this if you want to resolve the NTP Pool address to individual IPs yourself
     *
     * See https://github.com/instacart/truetime-android/issues/42
     * to understand why you may want to do something like this.
     *
     * @param resolvedNtpAddresses list of resolved IP addresses for an NTP
     * @return Observable of detailed long[] containing most important parts of the actual NTP response
     * See RESPONSE_INDEX_ prefixes in {@link SntpClient} for details
     */
    public Observable<long[]> initializeNtp(List<InetAddress> resolvedNtpAddresses) {
        return Observable
              .from(resolvedNtpAddresses)
              .compose(performNtpAlgorithm());
    }

    /**
     * Transformer that takes in a pool of NTP addresses
     * Against each IP host we issue a UDP call and retrieve the best response using the NTP algorithm
     */
    private Transformer<InetAddress, long[]> performNtpAlgorithm() {
        return new Transformer<InetAddress, long[]>() {
            @Override
            public Observable<long[]> call(Observable<InetAddress> inetAddressObservable) {
                return inetAddressObservable
                      .map(new Func1<InetAddress, String>() {
                          @Override
                          public String call(InetAddress inetAddress) {
                              return inetAddress.getHostAddress();
                          }
                      })
                      .flatMap(bestResponseAgainstSingleIp(5))  // get best response from querying the ip 5 times
                      .take(5)                                  // take 5 of the best results
                      .toList()
                      .filter(new Func1<List<long[]>, Boolean>() {
                          @Override
                          public Boolean call(List<long[]> longs) {
                              return longs != null && longs.size() > 0;
                          }
                      })
                      .map(filterMedianResponse())
                      .doOnNext(new Action1<long[]>() {
                          @Override
                          public void call(long[] ntpResponse) {
                              cacheTrueTimeInfo(ntpResponse);
                              saveTrueTimeInfoToDisk();
                          }
                      });
            }
        };
    }

    private Transformer<String, InetAddress> resolveNtpPoolToIpAddresses() {
        return new Transformer<String, InetAddress>() {
            @Override
            public Observable<InetAddress> call(Observable<String> ntpPoolObservable) {
                return ntpPoolObservable
                      .observeOn(Schedulers.io())
                      .flatMap(new Func1<String, Observable<InetAddress>>() {
                          @Override
                          public Observable<InetAddress> call(String ntpPoolAddress) {
                              try {
                                  TrueLog.d(TAG, "---- resolving ntpHost : " + ntpPoolAddress);
                                  return Observable.from(InetAddress.getAllByName(ntpPoolAddress));
                              } catch (UnknownHostException e) {
                                  return Observable.error(e);
                              }
                          }
                      });
            }
        };
    }

    private Func1<String, Observable<long[]>> bestResponseAgainstSingleIp(final int repeatCount) {
        return new Func1<String, Observable<long[]>>() {
            @Override
            public Observable<long[]> call(String singleIp) {
                return Observable
                      .just(singleIp)
                      .repeat(repeatCount)
                      .flatMap(new Func1<String, Observable<long[]>>() {
                          @Override
                          public Observable<long[]> call(final String singleIpHostAddress) {
                              return Observable
                                    .fromCallable(new Callable<long[]>() {
                                        @Override
                                        public long[] call() throws Exception {
                                            TrueLog.d(TAG, "---- requestTime from: " + singleIpHostAddress);
                                            return requestTime(singleIpHostAddress);
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .doOnError(new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            TrueLog.e(TAG, "---- Error requesting time", throwable);
                                        }
                                    })
                                    .retry(_retryCount);
                          }
                      })
                      .toList()
                      .onErrorResumeNext(Observable.<List<long[]>>empty())
                      .map(filterLeastRoundTripDelay()); // pick best response for each ip
            }
        };
    }

    private Func1<List<long[]>, long[]> filterLeastRoundTripDelay() {
        return new Func1<List<long[]>, long[]>() {
            @Override
            public long[] call(List<long[]> responseTimeList) {
                Collections.sort(responseTimeList, new Comparator<long[]>() {
                    @Override
                    public int compare(long[] lhsParam, long[] rhsLongParam) {
                        long lhs = SntpClient.getRoundTripDelay(lhsParam);
                        long rhs = SntpClient.getRoundTripDelay(rhsLongParam);
                        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
                    }
                });

                TrueLog.d(TAG, "---- filterLeastRoundTrip: " + responseTimeList);

                return responseTimeList.get(0);
            }
        };
    }

    private Func1<List<long[]>, long[]> filterMedianResponse() {
        return new Func1<List<long[]>, long[]>() {
            @Override
            public long[] call(List<long[]> bestResponses) {
                Collections.sort(bestResponses, new Comparator<long[]>() {
                    @Override
                    public int compare(long[] lhsParam, long[] rhsParam) {
                        long lhs = SntpClient.getClockOffset(lhsParam);
                        long rhs = SntpClient.getClockOffset(rhsParam);
                        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
                    }
                });

                TrueLog.d(TAG, "---- bestResponse: " + Arrays.toString(bestResponses.get(bestResponses.size() / 2)));

                return bestResponses.get(bestResponses.size() / 2);
            }
        };
    }
}

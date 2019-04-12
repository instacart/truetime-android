package com.instacart.library.truetime

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Arrays
import java.util.Collections
import java.util.Date

@Suppress("MemberVisibilityCanBePrivate", "unused")
class TrueTimeRx : TrueTime() {

    companion object {

        private val RX_INSTANCE = TrueTimeRx()
        private val TAG = TrueTimeRx::class.java.simpleName

        @JvmStatic fun now(): Date {
            return TrueTime.now()
        }

        @JvmStatic fun isInitialized(): Boolean {
            return TrueTime.isInitialized()
        }

        @JvmStatic fun build(): TrueTimeRx {
            return RX_INSTANCE
        }
    }

    private var retryCount = 50

    override fun withSharedPreferencesCache(context: Context): TrueTimeRx {
        super.withSharedPreferencesCache(context)
        return this
    }

    /**
     * Provide your own cache interface to cache the true time information.
     * @param cacheInterface the customized cache interface to save the true time data.
     */
    override fun withCustomizedCache(cacheInterface: CacheInterface): TrueTimeRx {
        super.withCustomizedCache(cacheInterface)
        return this
    }

    override fun withConnectionTimeout(timeoutInMillis: Int): TrueTimeRx {
        super.withConnectionTimeout(timeoutInMillis)
        return this
    }

    override fun withRootDelayMax(rootDelayMax: Float): TrueTimeRx {
        super.withRootDelayMax(rootDelayMax)
        return this
    }

    override fun withRootDispersionMax(rootDispersionMax: Float): TrueTimeRx {
        super.withRootDispersionMax(rootDispersionMax)
        return this
    }

    override fun withServerResponseDelayMax(serverResponseDelayInMillis: Int): TrueTimeRx {
        super.withServerResponseDelayMax(serverResponseDelayInMillis)
        return this
    }

    override fun withLoggingEnabled(isLoggingEnabled: Boolean): TrueTimeRx {
        super.withLoggingEnabled(isLoggingEnabled)
        return this
    }

    fun withRetryCount(retryCount: Int): TrueTimeRx {
        this.retryCount = retryCount
        return this
    }

    /**
     * Initialize TrueTime
     * See [.initializeNtp] for details on working
     *
     * @return accurate NTP Date
     */
    fun initializeRx(ntpPoolAddress: String): Single<Date> {
        return if (TrueTime.isInitialized()) {
            Single.just(TrueTime.now())
        } else {
            initializeNtp(ntpPoolAddress).map { TrueTime.now() }
        }
    }

    /**
     * Initialize TrueTime
     * A single NTP pool server is provided.
     * Using DNS we resolve that to multiple IP hosts (See [.initializeNtp] for manually resolved IPs)
     *
     * Use this instead of [.initializeRx] if you wish to also get additional info for
     * instrumentation/tracking actual NTP response data
     *
     * @param ntpPool NTP pool server e.g. time.apple.com, 0.us.pool.ntp.org
     * @return Observable of detailed long[] containing most important parts of the actual NTP response
     * See RESPONSE_INDEX_ prefixes in [SntpClient] for details
     */
    fun initializeNtp(ntpPool: String): Single<LongArray> {
        return Flowable
            .just(ntpPool)
            .compose(resolveNtpPoolToIpAddresses())
            .compose(performNtpAlgorithm())
            .firstOrError()
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
     * See RESPONSE_INDEX_ prefixes in [SntpClient] for details
     */
    fun initializeNtp(resolvedNtpAddresses: List<InetAddress>): Single<LongArray> {
        return Flowable.fromIterable(resolvedNtpAddresses)
            .compose(performNtpAlgorithm())
            .firstOrError()
    }

    /**
     * Transformer that takes in a pool of NTP addresses
     * Against each IP host we issue a UDP call and retrieve the best response using the NTP algorithm
     */
    private fun performNtpAlgorithm(): FlowableTransformer<InetAddress, LongArray> {
        return FlowableTransformer { inetAddressObservable ->
            inetAddressObservable
                .map { inetAddress -> inetAddress.hostAddress }
                .flatMap(bestResponseAgainstSingleIp(5))  // get best response from querying the ip 5 times
                .take(5)                                  // take 5 of the best results
                .toList()
                .toFlowable()
                .filter { longs -> longs.size > 0 }
                .map(filterMedianResponse())
                .doOnNext { ntpResponse ->
                    cacheTrueTimeInfo(ntpResponse)
                    TrueTime.saveTrueTimeInfoToDisk()
                }
        }
    }

    private fun resolveNtpPoolToIpAddresses(): FlowableTransformer<String, InetAddress> {
        return FlowableTransformer { ntpPoolFlowable ->
            ntpPoolFlowable
                .observeOn(Schedulers.io())
                .flatMap(Function<String, Flowable<InetAddress>> { ntpPoolAddress ->
                    try {
                        TrueLog.d(TAG, "---- resolving ntpHost : $ntpPoolAddress")
                        return@Function Flowable.fromArray(*InetAddress.getAllByName(ntpPoolAddress))
                    } catch (e: UnknownHostException) {
                        return@Function Flowable.error(e)
                    }
                })
        }
    }

    private fun bestResponseAgainstSingleIp(repeatCount: Int): Function<String, Flowable<LongArray>> {
        return Function { singleIp ->
            Flowable
                .just(singleIp)
                .repeat(repeatCount.toLong())
                .flatMap { singleIpHostAddress ->
                    Flowable.create(FlowableOnSubscribe<LongArray> { o ->
                        TrueLog.d(
                            TAG,
                            "---- requestTime from: $singleIpHostAddress"
                        )
                        try {
                            o.onNext(requestTime(singleIpHostAddress))
                            o.onComplete()
                        } catch (e: IOException) {
                            o.tryOnError(e)
                        }
                    }, BackpressureStrategy.BUFFER)
                        .subscribeOn(Schedulers.io())
                        .doOnError { throwable -> TrueLog.e(TAG, "---- Error requesting time", throwable) }
                        .retry(retryCount.toLong())
                }
                .toList()
                .toFlowable()
                .map(filterLeastRoundTripDelay()) // pick best response for each ip
        }
    }

    private fun filterLeastRoundTripDelay(): Function<List<LongArray>, LongArray> {
        return Function { responseTimeList ->
            Collections.sort(responseTimeList) { lhsParam, rhsLongParam ->
                val lhs = SntpClient.getRoundTripDelay(lhsParam)
                val rhs = SntpClient.getRoundTripDelay(rhsLongParam)
                if (lhs < rhs) -1 else if (lhs == rhs) 0 else 1
            }

            TrueLog.d(TAG, "---- filterLeastRoundTrip: $responseTimeList")

            responseTimeList[0]
        }
    }

    private fun filterMedianResponse(): Function<List<LongArray>, LongArray> {
        return Function { bestResponses ->
            Collections.sort(bestResponses) { lhsParam, rhsParam ->
                val lhs = SntpClient.getClockOffset(lhsParam)
                val rhs = SntpClient.getClockOffset(rhsParam)
                if (lhs < rhs) -1 else if (lhs == rhs) 0 else 1
            }

            TrueLog.d(TAG, "---- bestResponse: " + Arrays.toString(bestResponses[bestResponses.size / 2]))

            bestResponses[bestResponses.size / 2]
        }
    }
}

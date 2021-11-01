package com.instacart.library.truetime.time

import kotlinx.coroutines.Job
import java.util.Date

interface TrueTime2 {

    /**
     * The main 
     */
    fun initialize(with: TrueTimeParameters = TrueTimeParameters()): Date

    /**
     * Is [TrueTime2] successfully initialized with [initialize]
     */
    fun initialized(): Boolean

    /**
     * Keep running the [initialize] SNTP call in the background
     * to account for clock drift and update the locally stored SNTP result
     *
     * @param with Frequency for making the call is taken from [TrueTimeParameters.syncIntervalInMillis]
     * @return Use this Coroutines job to cancel the [sync] and all background work
     */
    suspend fun sync(with: TrueTimeParameters): Job
//
//    /**
//     * return the [Date] now if [TrueTime2] initialized
//     * otherwise [initialize] first, then return the time
//     */
//    suspend fun now(with: TrueTimeParameters? = null): Date {
//        return if (initialized()) {
//            nowForced()
//        } else {
//            initialize(with ?: TrueTimeParameters())
//        }
//    }

    /**
     * return the current time as calculated by TrueTime.
     * If not initialized, will throw [IllegalStateException]
     */
    @Throws(IllegalStateException::class)
    fun nowTrueOnly(): Date

    /**
     * return [nowTrueOnly] if TrueTime is available otherwise fallback to System clock date
     */
    fun nowSafely(): Date = if (initialized()) nowTrueOnly() else Date()
}

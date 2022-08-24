package com.instacart.truetime.time

import kotlinx.coroutines.Job
import java.util.Date

interface TrueTime {

    /**
     * Keep running the [com.instacart.truetime.sntp.Sntp.requestTime] call
     * in the background to account for clock drift and
     * update the locally stored SNTP result
     *
     * @param params Frequency for making the call is taken from [TrueTimeParameters.syncIntervalInMillis]
     * @return Use this Coroutines job to cancel the [sync] and all background work
     */
    suspend fun sync(params: TrueTimeParameters): Job

    fun hasTheTime(): Boolean

    /**
     * You should use this function by default to get the time
     * It respects [TrueTimeParameters.shouldReturnSafely] and returns accordingly
     */
    fun now(): Date

    /**
     * return the current time as calculated by TrueTime.
     * If not initialized, will throw [IllegalStateException]
     * now - but true time only
     */
    @Throws(IllegalStateException::class)
    fun nowTrueOnly(): Date

    /**
     * return [nowTrueOnly] if TrueTime is available otherwise fallback to System clock date
     */
    fun nowSafely(): Date = if (hasTheTime()) nowTrueOnly() else Date()
}

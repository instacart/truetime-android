package com.instacart.truetime.time

import kotlinx.coroutines.Job
import java.util.Date

interface TrueTime {

    /**
     * The main
     */
    fun initialize(with: TrueTimeParameters = TrueTimeParameters()): Date

    /**
     * Is [TrueTime] successfully initialized with [initialize]
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
    fun nowSafely(): Date = if (initialized()) nowTrueOnly() else Date()
}

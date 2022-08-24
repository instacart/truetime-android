package com.instacart.truetime.time

import kotlinx.coroutines.Job
import java.util.Date

interface TrueTime {

    /**
     * Keep running the [com.instacart.truetime.sntp.Sntp.requestTime] call
     * in the background to account for clock drift and
     * update the locally stored SNTP result
     *
     * @return Use this Coroutines job to cancel the [sync] and all background work
     */
    fun sync(): Job

    fun hasTheTime(): Boolean

    /**
     * This is [TrueTime]'s main function to get time
     * It should respect [TrueTimeParameters.shouldReturnSafely] setting
     */
    fun now(): Date

    /**
     * return the current time as calculated by TrueTime.
     * If TrueTime doesn't [hasTheTime], will throw [IllegalStateException]
     */
    @Throws(IllegalStateException::class)
    fun nowTrueOnly(): Date

    /**
     * return [nowTrueOnly] if TrueTime is available otherwise fallback to System clock date
     */
    fun nowSafely(): Date = if (hasTheTime()) nowTrueOnly() else Date()
}

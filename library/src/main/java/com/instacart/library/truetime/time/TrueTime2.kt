package com.instacart.library.truetime.time

import java.util.Date

interface TrueTime2 {

    suspend fun initialize(with: TrueTimeParameters = TrueTimeParameters()): Date

    /**
     * Was [TrueTime2] initialized by calling [initialize]
     */
    fun initialized(): Boolean

    /**
     * return the current time as calculated by TrueTime.
     * If not initialized, will throw [IllegalStateException]
     */
    @Throws(IllegalStateException::class)
    fun nowForced(): Date

    /**
     * return the [Date] now if [TrueTime2] initialized
     * otherwise [initialize] first, then return the time
     */
    suspend fun now(with: TrueTimeParameters? = null): Date {
        return if (initialized()) {
            nowForced()
        } else {
            initialize(with ?: TrueTimeParameters())
        }
    }

    /**
     * return [nowForced] if TrueTime is available otherwise fallback to System clock date
     */
    fun nowSafely(): Date = if (initialized()) nowForced() else Date()
}

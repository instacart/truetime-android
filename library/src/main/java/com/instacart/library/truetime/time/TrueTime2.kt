package com.instacart.library.truetime.time

import java.util.Date

interface TrueTime2 {

    fun initialize(with: TrueTimeParameters = TrueTimeParameters())

    fun now(): Date

    /**
     * return [now] if TrueTime is available otherwise
     * fallback to System clock date
     */
    fun nowSafely(): Date
}

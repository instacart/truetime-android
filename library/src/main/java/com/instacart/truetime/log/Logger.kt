package com.instacart.truetime.log

interface Logger {
    fun v(tag: String, msg: String)
    fun d(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String, t: Throwable?)
}

object LoggerNoOp : Logger {
    override fun v(tag: String, msg: String) {
        // no-op
    }

     override fun d(tag: String, msg: String) {
        // no-op
    }

    override fun i(tag: String, msg: String) {
        // no-op
    }

    override fun w(tag: String, msg: String) {
        // no-op
    }

    override fun e(tag: String, msg: String, t: Throwable?) {
        // no-op
    }
}

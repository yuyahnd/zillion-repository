package com.asterlist.androidsamples.camera

import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class ThreadHandler(name: String) {
    companion object {
        private const val TAG = "ThreadHandler"
    }

    private var mName = name
    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null

    fun post(runnable: () -> Unit) {
        mHandler?.post {
            run(runnable)
        }
    }

    fun startThread(): Handler {
        stopThread()
        mHandlerThread = HandlerThread(mName).also { it.start() }
        mHandler = Handler(mHandlerThread!!.looper)
        return mHandler!!
    }

    fun stopThread() {
        mHandlerThread?.quitSafely()
        try {
            mHandlerThread?.join()
            mHandlerThread = null
            mHandler = null
        } catch (e: InterruptedException) {
            Log.w(TAG, "Error: ", e)
        }
    }
}
package com.asterlist.androidsamples.service.foreground

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
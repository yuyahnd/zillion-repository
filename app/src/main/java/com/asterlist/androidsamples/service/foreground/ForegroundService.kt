package com.asterlist.androidsamples.service.foreground

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException()
    }
}
package com.asterlist.androidsamples.service.bind

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast

class BindService : Service() {
    companion object {
        private const val TAG = "BindService"
        const val MSG_SAY_HELLO = 1
        const val MSG_FINISH = 1000
    }

    private lateinit var mMessenger: Messenger

    internal class IncomingHandler(bindService: BindService): Handler() {
        private val bindService = bindService
        private val applicationContext: Context = bindService.applicationContext

        override fun handleMessage(msg: Message) {
            Log.d(TAG, "handleMessage")
            when(msg.what) {
                MSG_SAY_HELLO -> {
                    bindService.sayHello()
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
            try {
                msg.replyTo?.send(Message.obtain(null, MSG_FINISH, "完了しました"))
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }

    private fun sayHello() {
        Toast.makeText(applicationContext, "HELLO!", Toast.LENGTH_SHORT).show()
    }

}
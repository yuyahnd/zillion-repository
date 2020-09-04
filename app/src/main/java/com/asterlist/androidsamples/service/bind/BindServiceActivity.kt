package com.asterlist.androidsamples.service.bind

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.asterlist.androidsamples.R

class BindServiceActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "BindServiceActivity"
    }

    private var mService: Messenger? = null
    private var mReplyMessenger: Messenger? = null
    private var mBound: Boolean = false

    private lateinit var mBindButton: Button

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            mReplyMessenger = Messenger(ReplyHandler(applicationContext))
            mBound = true
        }

        override fun onServiceDisconnected(componentNamep0: ComponentName?) {
            mService = null
            mReplyMessenger = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_service)

        mBindButton = findViewById(R.id.service_button)
        mBindButton.setOnClickListener {
            callService()
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BindService::class.java).also {intent ->
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
    }

    private fun callService() {
        if (!mBound) return
        val msg = Message.obtain(null, BindService.MSG_SAY_HELLO, 0, 0)
        msg.replyTo = mReplyMessenger
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    class ReplyHandler(context: Context) : Handler() {
        private val mContext = context

        override fun handleMessage(msg: Message) {
            Log.d(TAG, "handleMessage")
            when(msg.what) {
                BindService.MSG_FINISH -> {
                    Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
package com.asterlist.android.samples.aar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class AarActivity : AppCompatActivity() {
    companion object {
        private val TAG = "AarActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Call onCreate")
    }
}
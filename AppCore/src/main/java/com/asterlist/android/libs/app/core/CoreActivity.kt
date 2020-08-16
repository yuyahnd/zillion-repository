package com.asterlist.android.libs.app.core

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class CoreActivity : AppCompatActivity() {

    companion object {
        private val TAG = "CoreActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {Log.d(TAG, "onCreate")}
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.DEBUG) {Log.d(TAG, "onStart")}
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) {Log.d(TAG, "onResume")}
    }

    override fun onPause() {
        super.onPause()
        if (BuildConfig.DEBUG) {Log.d(TAG, "onPause")}
    }

    override fun onStop() {
        super.onStop()
        if (BuildConfig.DEBUG) {Log.d(TAG, "onStop")}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) {Log.d(TAG, "onDestroy")}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (BuildConfig.DEBUG) {Log.d(TAG, "onSaveInstanceState")}
    }

    fun replaceFragment(containerViewId: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(containerViewId, fragment)
        transaction.commit()
    }
}
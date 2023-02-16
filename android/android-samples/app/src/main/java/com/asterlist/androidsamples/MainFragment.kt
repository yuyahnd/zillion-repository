package com.asterlist.androidsamples

import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat

class MainFragment : PreferenceFragmentCompat() {
    companion object {
        const val TAG = "MainFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main, rootKey)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "call onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "call onPause")
    }
}
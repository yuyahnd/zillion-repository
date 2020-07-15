package com.asterlist.android.opencv

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class MainFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main, rootKey)
    }
}
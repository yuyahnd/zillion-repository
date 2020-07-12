package com.asterlist.androidsamples

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, MainFragment())
        transaction.commit()
    }
}
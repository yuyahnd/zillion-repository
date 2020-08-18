package com.asterlist.androidsamples.camera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asterlist.androidsamples.R

class CustomCameraActivity : AppCompatActivity() {

    private lateinit var mCustomCameraFragment: CustomCameraFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, createCustomCameraFragment())
        transaction.commit()
    }

    private fun createCustomCameraFragment(): CustomCameraFragment {
        mCustomCameraFragment = CustomCameraFragment()
        mCustomCameraFragment.cameraId = "0"

        return mCustomCameraFragment
    }
}
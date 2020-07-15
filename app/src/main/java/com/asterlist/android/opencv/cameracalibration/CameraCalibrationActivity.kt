package com.asterlist.android.opencv.cameracalibration

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface

class CameraCalibrationActivity : CameraActivity(), OnTouchListener {
    companion object {
        private const val TAG = "CameraCalibrationActivity"
    }

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase

    private var mLoaderCallback = object : BaseLoaderCallback(this) {
        @SuppressLint("LongLogTag")
        override fun onManagerConnected(status: Int) {
            when(status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                    mOpenCvCameraView.setOnTouchListener(this@CameraCalibrationActivity)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }
}
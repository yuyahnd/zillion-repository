package com.asterlist.android.opencv.cameracalibration

import org.opencv.core.Size

class CameraCalibrator {
    companion object {
        private const val TAG = "OCV::CameraCalibrator"
    }

    private var mImageSize: Size? = null

    constructor(width: Int, height: Int) {
        mImageSize = Size(width.toDouble(), height.toDouble())
    }
}

package com.asterlist.android.opencv.cameracalibration

import org.opencv.calib3d.Calib3d
import org.opencv.core.Size

class CameraCalibrator {
    companion object {
        private const val TAG = "OCV::CameraCalibrator"
    }

    private var mFlags = 0
    private var mImageSize: Size? = null

    constructor(width: Int, height: Int) {
        mImageSize = Size(width.toDouble(), height.toDouble())
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5
    }
}

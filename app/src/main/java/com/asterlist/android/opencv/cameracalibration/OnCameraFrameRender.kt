package com.asterlist.android.opencv.cameracalibration

import android.content.res.Resources
import com.asterlist.android.opencv.R
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

abstract class FrameRender {
    lateinit var mCalibrator: CameraCalibrator
    abstract fun render(inputFrame: CvCameraViewFrame): Mat
}

class PreviewFrameRender : FrameRender() {

    override fun render(inputFrame: CvCameraViewFrame): Mat {
        return inputFrame.rgba()
    }
}

class CalibrationFrameRender : FrameRender {

    constructor(calibrator: CameraCalibrator) {
        mCalibrator = calibrator
    }

    override fun render(inputFrame: CvCameraViewFrame): Mat {
        var rgbaFrame = inputFrame.rgba()
        var grayFrame = inputFrame.gray()
        mCalibrator.processFrame(grayFrame, rgbaFrame)

        return rgbaFrame
    }
}

class UndistortionFrameRender : FrameRender {

    constructor(calibrator: CameraCalibrator) {
        mCalibrator = calibrator
    }

    override fun render(inputFrame: CvCameraViewFrame): Mat {
        var renderedFrame = Mat(inputFrame.rgba().size(), inputFrame.rgba().type())
        Calib3d.undistort(inputFrame.rgba(), renderedFrame,
                mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())
        return renderedFrame
    }
}

class ComparisonFrameRender : FrameRender {
    private var mWidth: Int
    private var mHeight: Int
    private var mResources : Resources

    constructor(calibrator: CameraCalibrator, width: Int, height: Int, resources: Resources) {
        mCalibrator = calibrator
        mWidth = width
        mHeight = height
        mResources = resources
    }

    override fun render(inputFrame: CvCameraViewFrame): Mat {
        var undistortedFrame = Mat(inputFrame.rgba().size(), inputFrame.rgba().type())
        Calib3d.undistort(inputFrame.rgba(), undistortedFrame,
                mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients());

        var comparisonFrame = inputFrame.rgba()
        undistortedFrame.colRange(Range(0, mWidth / 2)).copyTo(comparisonFrame.colRange(Range(mWidth / 2, mWidth)))
        var border : MutableList<MatOfPoint> = ArrayList()
        val shift = (mWidth * 0.005).toInt()
        border.add(
            MatOfPoint(
                Point((mWidth / 2 - shift).toDouble(), 0.0),
                Point((mWidth / 2 + shift).toDouble(), 0.0),
                Point((mWidth / 2 + shift).toDouble(), mHeight.toDouble()),
                Point((mWidth / 2 - shift).toDouble(), mHeight.toDouble())
            )
        )
        Imgproc.fillPoly(comparisonFrame, border, Scalar(255.0, 255.0, 255.0))

        Imgproc.putText(
            comparisonFrame,
            mResources.getString(R.string.original),
            Point(mWidth * 0.1, mHeight * 0.1),
            Imgproc.FONT_HERSHEY_SIMPLEX,
            1.0,
            Scalar(255.0, 255.0, 0.0)
        )
        Imgproc.putText(
            comparisonFrame,
            mResources.getString(R.string.undistorted),
            Point(mWidth * 0.6, mHeight * 0.1),
            Imgproc.FONT_HERSHEY_SIMPLEX,
            1.0,
            Scalar(255.0, 255.0, 0.0)
        )

        return comparisonFrame
    }
}

class OnCameraFrameRender {
    private var mFrameRender: FrameRender

    constructor(frameRender: FrameRender) {
        mFrameRender = frameRender
    }

    fun render(inputFrame: CvCameraViewFrame): Mat {
        return mFrameRender.render(inputFrame)
    }
}
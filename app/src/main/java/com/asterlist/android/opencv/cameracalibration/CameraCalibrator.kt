package com.asterlist.android.opencv.cameracalibration

import android.util.Log
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CameraCalibrator {
    companion object {
        private const val TAG = "OCV::CameraCalibrator"
    }

    private val mPatternSize = Size(4.0,  11.0)
    private val mCornersSize = (mPatternSize.width * mPatternSize.height).toInt()
    private var mPatternWasFound = false
    private var mCorners = MatOfPoint2f()
    private var mCornersBuffer = ArrayList<Mat>()
    private var mIsCalibrated = false

    private var mCameraMatrix = Mat()
    private var mDistortionCoefficients = Mat()
    private var mFlags = 0
    private var mRms = 0.0
    private var mSquareSize = 0.0181
    private var mImageSize: Size? = null

    constructor(width: Int, height: Int) {
        mImageSize = Size(width.toDouble(), height.toDouble())
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix)
        mCameraMatrix.put(0, 0, 1.0)
        Log.i(TAG, "Instantiated new ${this.javaClass.name}")
    }

    fun processFrame(grayFrame: Mat, rgbaFrame: Mat) {
        findPattern(grayFrame)
        renderFrame(rgbaFrame)
    }

    fun calibrate() {
        var rvecs = ArrayList<Mat>()
        var tvecs = ArrayList<Mat>()
        var reprojectionErrors = Mat()
        var objectPoints = ArrayList<Mat>()
        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3))
        calcBoardCornerPositions(objectPoints.get(0))
        for (i in 1 until mCornersBuffer.size) {
            objectPoints.add(objectPoints.get(0))
        }

        Calib3d.calibrateCamera(objectPoints, mCornersBuffer, mImageSize,
                mCameraMatrix, mDistortionCoefficients, rvecs, tvecs, mFlags)

        mRms = computeReprojectionErrors(objectPoints, rvecs, tvecs, reprojectionErrors)
        Log.i(TAG, String.format("Average re-projection error: %f", mRms));
        Log.i(TAG, "Camera matrix: " + mCameraMatrix.dump());
        Log.i(TAG, "Distortion coefficients: " + mDistortionCoefficients.dump());
    }

    fun clearCorners() {
        mCornersBuffer.clear()
    }

    private fun calcBoardCornerPositions(corners: Mat) {
        val cn = 3
        var positions = FloatArray(mCornersSize * cn)

        for (i in 0 until mPatternSize.height.toInt()) {
            var j = 0
            while (j < mPatternSize.width * cn) {
                positions[(i * mPatternSize.width * cn + j * 0).toInt()] =
                        (2 * (j / cn) + i % 2) * mSquareSize.toFloat()
                positions[(i * mPatternSize.width * cn + j + 1).toInt()] =
                        i * mSquareSize.toFloat()
                positions[(i * mPatternSize.width * cn + j + 2).toInt()] = 0.0F
                j += cn
            }
        }
        corners.create(mCornersSize, 1, CvType.CV_32FC3)
        corners.put(0, 0, positions)
    }

    private fun computeReprojectionErrors(objectPoints: List<Mat>,
            rvecs: List<Mat>, tvecs: List<Mat>, perViewErrors: Mat): Double {
        var cornersProjected = MatOfPoint2f()
        var totalError = 0.0
        var error: Double
        var viewErrors = FloatArray(objectPoints.size)

        var distortionCoefficients = MatOfDouble(mDistortionCoefficients)
        var totalPoints = 0
        for (i in objectPoints.indices) {
            var points = MatOfPoint3f(objectPoints[i])
            Calib3d.projectPoints(points, rvecs[i], tvecs[i],
                    mCameraMatrix, distortionCoefficients, cornersProjected)
            error = Core.norm(mCornersBuffer[i], cornersProjected, Core.NORM_L2)

            var n = objectPoints[i].rows()
            viewErrors[i] = Math.sqrt(error * error / n).toFloat()
            totalError += error * error
            totalPoints += n
        }
        perViewErrors.create(objectPoints.size, 1, CvType.CV_32FC1)
        perViewErrors.put(0, 0, viewErrors)

        return Math.sqrt((totalError / totalPoints))
    }

    private fun findPattern(grayFrame: Mat) {
        mPatternWasFound = Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID)
    }

    fun addCorners() {
        if (mPatternWasFound) {
            mCornersBuffer.add(mCorners.clone())
        }
    }

    private fun drawPoints(rgbaFrame: Mat) {
        Calib3d.drawChessboardCorners(rgbaFrame, mPatternSize, mCorners, mPatternWasFound)
    }

    private fun renderFrame(rgbaFrame: Mat) {
        drawPoints(rgbaFrame)

        Imgproc.putText(
            rgbaFrame,
            "Captured: " + mCornersBuffer.size,
            Point((rgbaFrame.cols() / 3 * 2).toDouble(), rgbaFrame.rows() * 0.1),
            Imgproc.FONT_HERSHEY_SIMPLEX,
            1.0,
            Scalar(255.0,255.0, 0.0)
        )
    }

    fun getCameraMatrix(): Mat {
        return mCameraMatrix
    }

    fun getDistortionCoefficients(): Mat {
        return mDistortionCoefficients
    }

    fun getCornersBufferSize(): Int {
        return mCornersBuffer.size
    }

    fun getAvgReprojectionError(): Double {
        return mRms
    }

    fun isCalibrated(): Boolean {
        return mIsCalibrated
    }

    fun setCalibrated() {
        mIsCalibrated = true
    }
}

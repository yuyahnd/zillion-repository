package com.asterlist.android.opencv.cameracalibration

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Toast
import com.asterlist.android.opencv.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.Mat
import java.util.*

class CameraCalibrationActivity : CameraActivity(), CvCameraViewListener2, OnTouchListener {
    companion object {
        private const val TAG = "CameraCalibrationActivity"
    }

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private lateinit var mCalibrator: CameraCalibrator
    private lateinit var mOnCameraFrameRender: OnCameraFrameRender
    private var mMenu: Menu? = null
    private var mWidth = 0
    private var mHeight = 0

    val mCoroutineScope = CoroutineScope(Dispatchers.Default)

    private var mLoaderCallback = object : BaseLoaderCallback(this) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.requestFeature(Window.FEATURE_ACTION_BAR)

        setContentView(R.layout.camera_calibration_surface_view)

        mOpenCvCameraView = findViewById(R.id.camera_calibration_java_surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return Collections.singletonList(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.calibration, menu)
        mMenu = menu
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.preview_mode)?.isEnabled = true
        if (mCalibrator != null && !mCalibrator.isCalibrated()) {
            menu?.findItem(R.id.preview_mode)?.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.calibration -> {
                mOnCameraFrameRender = OnCameraFrameRender(CalibrationFrameRender(mCalibrator))
                item.isChecked = true
                true
            }
            R.id.undistortion -> {
                mOnCameraFrameRender = OnCameraFrameRender(UndistortionFrameRender(mCalibrator))
                item.isChecked = true
                true
            }
            R.id.comparison -> {
                mOnCameraFrameRender = OnCameraFrameRender(ComparisonFrameRender(mCalibrator, mWidth, mHeight, resources))
                item.isChecked = true
                true
            }
            R.id.calibrate -> {
                onOptionCalibrate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onOptionCalibrate() {
        val res = resources
        if (mCalibrator.getCornersBufferSize() < 2) {
            (Toast.makeText(this, res.getString(R.string.more_samples), Toast.LENGTH_SHORT)).show()
            return
        }

        mOnCameraFrameRender = OnCameraFrameRender(PreviewFrameRender())
        mCoroutineScope.launch {
            onOptionCalibrateTask()
        }
    }

    private suspend fun onOptionCalibrateTask() {
        // onPreExecute
        withContext(Dispatchers.Main) {
            Log.d(TAG,"onPreExecute")
        }

        // doInBackground
        mCalibrator.calibrate()

        // onPostExecute
        withContext(Dispatchers.Main) {
            mCalibrator.calibrate()
            mOnCameraFrameRender = OnCameraFrameRender(CalibrationFrameRender(mCalibrator))
            var resultMessage = if (mCalibrator.isCalibrated()) {
                resources.getString(R.string.calibration_successful).toString() + " " + mCalibrator.getAvgReprojectionError()
            } else {
                resources.getString(R.string.calibration_unsuccessful)
            }

            if (mCalibrator.isCalibrated()) {
               CalibrationResult.save(
                   this@CameraCalibrationActivity,
                   mCalibrator.getCameraMatrix(),
                   mCalibrator.getDistortionCoefficients()
               )
            }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        if (mWidth != width || mHeight != height) {
            mWidth = width
            mHeight = height
            mCalibrator = CameraCalibrator(mWidth, mHeight)
            if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated()
            } else {
                if (mMenu != null && !mCalibrator.isCalibrated()) {
                    mMenu!!.findItem(R.id.preview_mode).isEnabled = false
                }
            }

            mOnCameraFrameRender = OnCameraFrameRender(CalibrationFrameRender(mCalibrator))
        }
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        return mOnCameraFrameRender.render(inputFrame)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch invoked")

        mCalibrator.addCorners()
        return false
    }
}

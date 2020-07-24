package com.asterlist.android.opencv.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.asterlist.android.opencv.R

class CameraActivity : Activity() {
    companion object {
        private const val TAG = "Camera::Activity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    private val mImageConverter = ImageConverter()

    // プレビュー
    private lateinit var mPreviewView: TextureView
    private var mPreviewImageReader: ImageReader? = null
    private var mPreviewFormat = ImageFormat.YUV_420_888
    private var mImageReadThread: HandlerThread? = null
    private var mImageReadHandler: Handler? = null

    // カメラ
    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mPreviewRequest : CaptureRequest? = null
    private var mCaptureSession : CameraCaptureSession? = null

    private val mCameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }
        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraDevice?.close()
            mCameraDevice = null
        }
        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraDevice?.close()
            mCameraDevice = null
        }
    }

    private val mPreviewTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.d("TEST", "onSurfaceTextureAvailable")
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.d("TEST", "onSurfaceTextureSizeChanged")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            Log.d("TEST", "onSurfaceTextureUpdated")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Log.d("TEST", "onSurfaceTextureDestroyed")
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mPreviewView = findViewById<TextureView>(R.id.preview)
        mPreviewView.surfaceTextureListener = mPreviewTextureListener
    }

    override fun onStart() {
        Log.d("TEST", "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("TEST", "onResume")
        super.onResume()
        connectCamera()
    }

    override fun onPause() {
        Log.d("TEST", "onPause")
        super.onPause()
        disconnectCamera()
    }

    override fun onStop() {
        Log.d("TEST", "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("TEST", "onDestroy")
        super.onDestroy()
    }

    private fun connectCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var cameraPermission  = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            cameraManager.openCamera("0", mCameraStateCallback, startCameraThread())
        } catch (e : CameraAccessException) {
            Log.e(TAG, "openCamera Error : ", e)
        }
    }

    private fun disconnectCamera() {
        mCaptureSession?.close()
        mCaptureSession = null
        mCameraDevice?.close()
        mCameraDevice = null
        mPreviewImageReader?.close()
        mPreviewImageReader = null
        stopThread()
    }

    private fun createCameraPreviewSession() {
        val previewReader = createPreviewReader()
        val previewSurface = previewReader.surface
        val previewOutputConfiguration = OutputConfiguration(previewSurface)

        mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewRequestBuilder?.addTarget(previewSurface)

        val outputs = listOf(previewOutputConfiguration)

        val sessionConfiguration =
            SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputs,
                AsyncTask.SERIAL_EXECUTOR, mCaptureStateCallback)
        try {
            mCameraDevice?.createCaptureSession(sessionConfiguration)
        } catch (e: CameraAccessException) {

        }
    }

    private val mCaptureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            try {
                mCaptureSession = cameraCaptureSession
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(60, 60))
                mPreviewRequest = mPreviewRequestBuilder?.build()
                cameraCaptureSession.setRepeatingRequest(mPreviewRequest!!, null, mCameraHandler)
            } catch (e: CameraAccessException) {
            }
        }
        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
    }

    private fun createPreviewReader(): ImageReader {
        mPreviewImageReader = ImageReader.newInstance(3840, 2160, mPreviewFormat, 60)
        mPreviewImageReader?.setOnImageAvailableListener(mOnImageAvailableListener, startImageReadThread())
        return mPreviewImageReader!!
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { imageReader ->
        val image = imageReader.acquireLatestImage()
        Log.d("TEST", "Call onImageAvailable. Image = $image")
        if (image == null) {
            return@OnImageAvailableListener
        }
        var mat = Matrix()
        mat.postRotate(90F)

        var bitmapOrigne = mImageConverter.convertYuvToRgb(this@CameraActivity, image)
        var bitmap = Bitmap.createBitmap(bitmapOrigne, 0, 0, bitmapOrigne!!.width, bitmapOrigne!!.height, mat, true);

        var canvas = mPreviewView?.lockCanvas()
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(bitmap!!,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect((canvas.width - bitmap.width) / 2,
                (canvas.height - bitmap.height) / 2,
                (canvas.width - bitmap.width) / 2 + bitmap.width,
                (canvas.height - bitmap.height) / 2 + bitmap.height),
            null
        )
        mPreviewView.unlockCanvasAndPost(canvas)

        image?.close()
    }

    private fun startThread() {
        startCameraThread()
    }

    private fun stopThread() {
        stopCameraThread()
        stopImageReadThread()
    }

    private fun startCameraThread(): Handler {
        stopCameraThread()
        mCameraThread = HandlerThread("Camera Thread").also { it.start() }
        mCameraHandler = Handler(mCameraThread!!.looper)
        return mCameraHandler!!
    }

    private fun stopCameraThread() {
        mCameraThread?.quitSafely()
        try {
            mCameraThread?.join()
            mCameraThread = null
            mCameraHandler = null
        } catch (e: InterruptedException) {
            Log.w(TAG, "Error: ", e)
        }
    }

    private fun startImageReadThread(): Handler {
        stopImageReadThread()
        mImageReadThread = HandlerThread("ImageReader Thread").also { it.start() }
        mImageReadHandler = Handler(mImageReadThread!!.looper)
        return mImageReadHandler!!
    }

    private fun stopImageReadThread() {
        mImageReadThread?.quitSafely()
        try {
            mImageReadThread?.join()
            mImageReadThread = null
            mImageReadHandler = null
        } catch (e: InterruptedException) {
            Log.w(TAG, "Error: ", e)
        }
    }

    private fun haveCameraPermission(): Boolean {
        val cameraPermission  = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE)
    }
}
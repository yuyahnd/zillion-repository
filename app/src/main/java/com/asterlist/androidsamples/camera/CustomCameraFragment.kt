package com.asterlist.androidsamples.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asterlist.androidsamples.R

class CustomCameraFragment : Fragment() {
    companion object {
        private const val TAG = "CustomCameraFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1000
        private const val SURPLUS_IMAGES = 5
    }

    var cameraId = "0"
    var captureSize = Size(1920, 1080)
    var format = ImageFormat.YUV_420_888
    var maxImages = 30
    var fpsRange = Range(60, 60)

    var onImageAvailable: ((image: Image, imageList: ArrayList<Image>) -> Unit)? = null

    private lateinit var mTextureView: TextureView

    private var mCameraDevice: CameraDevice? = null
    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null

    private var mCaptureRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCaptureRequest: CaptureRequest? = null


    private var mImageReader: ImageReader? = null
    private var mImageReaderThread: HandlerThread? = null
    private var mImageReaderHandler: Handler? = null

    private lateinit var mCaptureList: ArrayList<Image>

    private var mImageHandler = ThreadHandler("Image Thread")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCaptureList = ArrayList<Image>(maxImages)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTextureView = view.findViewById(R.id.textureView)
    }

    override fun onResume() {
        super.onResume()
        mImageHandler.startThread()
        connectCamera(requireContext())
    }

    override fun onPause() {
        super.onPause()
        disconnectCamera()
        mImageHandler.stopThread()
    }

    private fun connectCamera(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var cameraPermission  = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            cameraManager.openCamera(cameraId, mCameraStateCallback, startCameraThread())
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Camera Access Error : ", e)
        }
    }

    private fun disconnectCamera() {
        mCameraDevice?.close()
        mCameraDevice = null
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE)
    }

    private val mCameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraDevice?.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraDevice?.close()
            mCameraDevice = null
        }
    }

    private fun createCameraPreviewSession() {
        val imageReader = createImageReader()
        val captureOutputConfiguration = OutputConfiguration(imageReader.surface)

        mCaptureRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mCaptureRequestBuilder?.addTarget(imageReader.surface)

        val outputs = listOf(captureOutputConfiguration)
        val sessionConfiguration = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            outputs,
            AsyncTask.SERIAL_EXECUTOR,
            mCaptureStateCallback
        )

        try {
            mCameraDevice?.createCaptureSession(sessionConfiguration)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "createCaptureSession Error : ", e)
        }
    }

    private val mCaptureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            try {
                mCaptureSession = cameraCaptureSession
                mCaptureRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                mCaptureRequestBuilder?.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
                mCaptureRequest = mCaptureRequestBuilder?.build()
                cameraCaptureSession.setRepeatingRequest(mCaptureRequest!!, null, mCameraHandler)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "onConfigured Error : ", e)
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
    }

    private fun createImageReader(): ImageReader {
        mImageReader = ImageReader.newInstance(captureSize.width, captureSize.height, format, maxImages + SURPLUS_IMAGES)
        mImageReader!!.setOnImageAvailableListener(mOnCaptureImageAvailableListener, startImageReaderThread())
        return mImageReader!!
    }

    private var mOnCaptureImageAvailableListener = ImageReader.OnImageAvailableListener {imageReader ->
        val image = imageReader.acquireNextImage() ?: return@OnImageAvailableListener

        mImageHandler.post {
            onImageAvailable?.invoke(image, mCaptureList)
        }

        if (maxImages < mCaptureList.size) {
            mCaptureList.removeAt(0)?.close()
        }
        mCaptureList.add(image)
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

    private fun startImageReaderThread(): Handler {
        stopImageReaderThread()
        mImageReaderThread = HandlerThread("ImageReader Thread").also { it.start() }
        mImageReaderHandler = Handler(mImageReaderThread!!.looper)
        return mImageReaderHandler!!
    }

    private fun stopImageReaderThread() {
        mImageReaderThread?.quitSafely()
        try {
            mImageReaderThread?.join()
            mImageReaderThread = null
            mImageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.w(TAG, "Error: ", e)
        }
    }
}
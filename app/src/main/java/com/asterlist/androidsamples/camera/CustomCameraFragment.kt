package com.asterlist.androidsamples.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
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
    }

    var cameraId = "0"
    var captureSize = Size(1920, 1080)
    var format = ImageFormat.YUV_420_888
    var maxImages = 30

    private lateinit var mtextureView: TextureView

    private var mCameraDevice: CameraDevice? = null
    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_custom_camera, container, false)
        mtextureView = container!!.findViewById(R.id.textureView)
        return view
    }

    override fun onResume() {
        super.onResume()
        connectCamera(requireContext())
    }

    override fun onPause() {
        super.onPause()
        disconnectCamera()
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
}
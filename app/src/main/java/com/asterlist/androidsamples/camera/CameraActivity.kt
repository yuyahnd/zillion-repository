package com.asterlist.androidsamples.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.asterlist.androidsamples.R
import java.util.*

class CameraActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Camera"
        const val REQUEST_PERMISSION = 1000
    }

    private lateinit var mPreviewView : TextureView
    private lateinit var mImageReader : ImageReader

    private var mbackgroundThread : HandlerThread? = null
    private var mbackgroundHandler : Handler? = null
    private var mCameraDevice : CameraDevice? = null
    private var mPreviewRequestBuilder : CaptureRequest.Builder? = null
    private var mPreviewRequest : CaptureRequest? = null
    private var mCaptureSession : CameraCaptureSession? = null

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2)
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return false
        }
    }

    private var mStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            createCameraPreviewSession()

        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            mCameraDevice = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        startBackgroundThread()

        mPreviewView = findViewById<TextureView>(R.id.textureView)
        mPreviewView.surfaceTextureListener = mSurfaceTextureListener
    }

    private fun openCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var cameraPermission  = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.openCamera(cameraId, mStateCallback, mbackgroundHandler)
        } catch (e : CameraAccessException) {

        }

    }

    private fun createCameraPreviewSession() {
        try {
            val texture = mPreviewView.surfaceTexture
            texture?.setDefaultBufferSize(mPreviewView.width, mPreviewView.height)

            val surface = Surface(texture)
            mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)

            mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        try {
                            mCaptureSession = cameraCaptureSession
                            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            mPreviewRequest = mPreviewRequestBuilder?.build()
                            cameraCaptureSession.setRepeatingRequest(mPreviewRequest!!, null, mbackgroundHandler)
                        } catch (e : CameraAccessException) {

                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        // NOP
                    }
                }, null)
        } catch (e : CameraAccessException) {

        }

    }


    private fun startBackgroundThread() {
        mbackgroundThread = HandlerThread("CameraBackground").also { it.start() }
        mbackgroundHandler = Handler(mbackgroundThread!!.looper)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_PERMISSION
        )
    }
}
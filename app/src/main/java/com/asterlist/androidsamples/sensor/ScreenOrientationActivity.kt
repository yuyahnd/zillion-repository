package com.asterlist.androidsamples.sensor

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import com.asterlist.androidsamples.R

class ScreenOrientationActivity : Activity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private val mAccelerometerReading = FloatArray(3)
    private val mMagnetometerReading = FloatArray(3)

    private val mRotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

    private lateinit var mAzimuthView: TextView
    private lateinit var mPitch: TextView
    private lateinit var mRoll: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_orientation)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAzimuthView = findViewById(R.id.azimuth)
        mPitch = findViewById(R.id.pitch)
        mRoll = findViewById(R.id.roll)
    }

    override fun onResume() {
        super.onResume()
        registerListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterListener()
    }

    fun registerListener() {
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            mSensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            mSensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun unregisterListener() {
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(event: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.size)
        }

        updateOrientationAngles()
        updateView()
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            mRotationMatrix,
            null,
            mAccelerometerReading,
            mMagnetometerReading
        )

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles)
    }

    private fun updateView() {
        mAzimuthView.setText("z : " + mOrientationAngles[0].toString())
        mPitch.setText(      "x : " + mOrientationAngles[1].toString())
        mRoll.setText(       "y : " + mOrientationAngles[2].toString())
    }
}
package com.asterlist.androidsamples.sensor

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class ScreenOrientationActivity : Activity(), SensorEventListener {

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }
}
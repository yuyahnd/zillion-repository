package com.asterlist.android.opencv.cameracalibration

import android.app.Activity
import android.content.Context
import android.util.Log
import org.opencv.core.Mat

abstract class CalibrationResult {
    companion object {
        private const val TAG = "OCV::CalibrationResult"

        private const val CAMERA_MATRIX_ROWS = 3
        private const val CAMERA_MATRIX_COLS = 3
        private const val DISTORTION_COEFFICIENTS_SIZE = 5

        fun save(activity: Activity, cameraMatrix: Mat, distortionCoefficients: Mat) {
            var sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            var editor = sharedPref.edit()

            var cameraMatrixArray = DoubleArray(CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS)
            cameraMatrix.get(0, 0, cameraMatrixArray)
            for (i in 0 until CAMERA_MATRIX_ROWS) {
                for (j in 0 until CAMERA_MATRIX_COLS) {
                    var id = i * CAMERA_MATRIX_ROWS + j
                    editor.putFloat(id.toString(), cameraMatrixArray[id].toFloat())
                }
            }

            var distortionCoefficientsArray = DoubleArray(DISTORTION_COEFFICIENTS_SIZE)
            distortionCoefficients.get(0, 0, distortionCoefficientsArray)
            var shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS
            for (i in shift until DISTORTION_COEFFICIENTS_SIZE + shift) {
                editor.putFloat(i.toString(), distortionCoefficientsArray[i - shift].toFloat())
            }

            editor.apply()
            Log.i(TAG, "Saved camera matrix: " + cameraMatrix.dump())
            Log.i(TAG, "Saved distortion coefficients: " + distortionCoefficients.dump())
        }

        fun tryLoad(activity: Activity, cameraMatrix: Mat, distortionCoefficients: Mat): Boolean {
            var sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            if (sharedPref.getFloat("0", -1F) == -1F) {
                Log.i(TAG, "No previous calibration results found");
                return false;
            }

            var cameraMatrixArray = DoubleArray(CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS)
            for (i in 0 until CAMERA_MATRIX_ROWS) {
                for (j in 0 until CAMERA_MATRIX_COLS) {
                    var id = i  * CAMERA_MATRIX_ROWS + j
                    cameraMatrixArray[id] = sharedPref.getFloat(id.toString(), -1F).toDouble()
                }
            }
            cameraMatrix.put(0, 0, *cameraMatrixArray)
            Log.i(TAG, "Loaded camera matrix: " + cameraMatrix.dump())

            var distortionCoefficientsArray = DoubleArray(DISTORTION_COEFFICIENTS_SIZE)
            var shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS
            for (i in shift until DISTORTION_COEFFICIENTS_SIZE + shift) {
                distortionCoefficientsArray[i - shift] = sharedPref.getFloat(i.toString(), -1F).toDouble()
            }
            distortionCoefficients.put(0, 0, *distortionCoefficientsArray)
            Log.i(TAG, "Loaded distortion coefficients: " + distortionCoefficients.dump())

            return true
        }
    }
}
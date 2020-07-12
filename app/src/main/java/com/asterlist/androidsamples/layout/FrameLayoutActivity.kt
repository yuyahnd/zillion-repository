package com.asterlist.androidsamples.layout

import android.graphics.BitmapFactory
import android.os.Bundle
import android.telecom.CallScreeningService
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.asterlist.androidsamples.R
import java.io.IOException

class FrameLayoutActivity : AppCompatActivity() {
    private val TAG = "FrameLayoutActivity"
    lateinit var fullScreen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_frame)

        fullScreen = findViewById<ImageView>(R.id.full_screen)
        try {
            resources.assets.open("img_sample_01.jpg").use {
                var bitmap = BitmapFactory.decodeStream(it)
                fullScreen.setImageBitmap(bitmap)
            }
        } catch (e : IOException) {
            Log.e(TAG, "Error : " + e.printStackTrace())
        }
    }
}
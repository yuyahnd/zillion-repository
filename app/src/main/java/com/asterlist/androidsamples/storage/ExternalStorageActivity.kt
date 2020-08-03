package com.asterlist.androidsamples.storage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.*
import java.nio.charset.StandardCharsets

class ExternalStorageActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION = 1000

    private lateinit var mFile : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()

        val path = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        mFile = File(path, "test.txt")
        Log.d("TEST", "file = ${mFile.absoluteFile}")
        // 出力先：/storage/emulated/0/Android/data/com.asterlist.androidsamples/files/Documents/test.txt
    }

    override fun onResume() {
        super.onResume()

        if (isExternalStorageWritable()) {
            writeFile()
        }
    }

    private fun writeFile() {
        try {
            FileOutputStream(mFile, true).use { fileOut ->
                OutputStreamWriter(fileOut, StandardCharsets.UTF_8).use {output ->
                    val buff = BufferedWriter(output)
                    buff.write("TEST WRITE")
                    buff.newLine()
                    buff.flush()
                    buff.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return
        }
        var permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION)
    }
}
package com.asterlist.android.opencv.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import java.nio.ByteBuffer

class ImageConverter {

    fun convertYuvToRgb(context: Context, image: Image): Bitmap? {
        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }
        val yuvBytes = convertYuvBuffer(image)

        val rs = RenderScript.create(context)
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

        val allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().size)
        allocationYuv.copyFrom(yuvBytes.array())

        var scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
        scriptYuvToRgb.setInput(allocationYuv)
        scriptYuvToRgb.forEach(allocationRgb)

        allocationRgb.copyTo(bitmap)

        allocationYuv.destroy()
        allocationRgb.destroy()
        rs.destroy()

        return bitmap
    }

    private fun convertYuvBuffer(image: Image): ByteBuffer {
        val crop = image.cropRect
        val width = crop.width()
        val height = crop.height()

        val rowData  = ByteArray(image.planes[0].rowStride)
        val bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
        val output = ByteBuffer.allocateDirect(bufferSize)

        var channelOffset = 0
        var outputStride = 0
        var index = 0

        while(index < 3) {
            when(index) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> {
                    channelOffset = width * height +1
                    outputStride = 2
                }
                2 -> {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer = image.planes[index].buffer
            val rowStride = image.planes[index].rowStride
            val pixelStride = image.planes[index].pixelStride

            val shift = if (index == 0) { 0 } else { 1 }
            val widthShifted  = width.ushr(shift)
            val heightShifted = height.ushr(shift)

            buffer.position(rowStride * (crop.top.ushr(shift)) + pixelStride * (crop.left.ushr(shift)))

            var row = 0
            while (row < heightShifted) {
                var length: Int = 0
                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted
                    buffer.get(output.array(), channelOffset, length)
                    channelOffset += length
                }
                else {
                    length = (widthShifted - 1) * pixelStride + 1
                    buffer.get(rowData, 0, length)

                    var col = 0
                    while(col < widthShifted) {
                        output.array()[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                        col++
                    }
                }

                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
                row++
            }
            index++
        }
        return output
    }

    fun resizeYuvImage(context: Context, image: Image): Bitmap? {
        var yBuffer = image.planes[0].buffer
        var uBuffer = image.planes[1].buffer
        var vBuffer = image.planes[2].buffer

        var ySize = yBuffer.remaining()
        var uSize = uBuffer.remaining()
        var vSize = vBuffer.remaining()

        var imageBytes = ByteArray(ySize + uSize + vSize)
        yBuffer.get(imageBytes, 0, ySize)
        vBuffer.get(imageBytes, ySize, vSize)
        uBuffer.get(imageBytes, ySize + vSize, uSize)

        var yuvBytes = resize(imageBytes, image)

        val rs = RenderScript.create(context)
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

        val allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().size)
        allocationYuv.copyFrom(yuvBytes.array())

        var scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
        scriptYuvToRgb.setInput(allocationYuv)
        scriptYuvToRgb.forEach(allocationRgb)

        allocationRgb.copyTo(bitmap)

        allocationYuv.destroy()
        allocationRgb.destroy()
        rs.destroy()

        return bitmap
    }

    private fun resize(imageBytes: ByteArray, image: Image): ByteBuffer {
        val yuv = ByteArray(image.width/2 * image.height/2 * 3 / 2)

        var i = 0
        for (y in 0 until image.height step 2) {
            for (x in 0 until image.height step 2) {
                yuv[i] = imageBytes[y * image.width + x]
                i++
            }
        }

        for (y in 0 until image.height / 2 step 2) {
            for (x in 0 until image.width step 4) {
                yuv[i] = imageBytes[(image.width * image.height) + (y * image.width) + x]
                i++
                yuv[i] = imageBytes[(image.width * image.height) + (y * image.width) + (x + 1)]
                i++
            }
        }
        return ByteBuffer.wrap(yuv)
    }
}
package com.wachirapong.kdocscan.util

import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ImageUtil {
    fun imageToBitmap(image: ImageProxy, rotationDegrees: Float): Bitmap {

        assert(image.format == ImageFormat.NV21)

        // NV21 is a plane of 8 bit Y values followed by interleaved  Cb Cr
        val ib = ByteBuffer.allocate(image.height * image.width * 2)

        val y = image.planes[0].buffer
        val cr = image.planes[1].buffer
        val cb = image.planes[2].buffer
        ib.put(y)
        ib.put(cb)
        ib.put(cr)

        val yuvImage = YuvImage(
            ib.array(),
            ImageFormat.NV21, image.width, image.height, null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(
                0, 0,
                image.width, image.height
            ), 50, out
        )
        val imageBytes = out.toByteArray()
        val bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        var bitmap = bm

        // On android the camera rotation and the screen rotation
        // are off by 90 degrees, so if you are capturing an image
        // in "portrait" orientation, you'll need to rotate the image.
        if (rotationDegrees != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bm,
                bm.width, bm.height, true
            )
            bitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix, true
            )
        }
        return bitmap
    }
}
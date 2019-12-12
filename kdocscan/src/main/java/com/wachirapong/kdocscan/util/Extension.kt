package com.wachirapong.kdocscan.util

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

fun Mat.toBitMap(destination: Bitmap) {
    Utils.matToBitmap(this, destination)
}

fun Bitmap.toMat(destination: Mat) {
    Utils.bitmapToMat(this, destination)
}
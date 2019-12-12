package com.wachirapong.kdocscan.data

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size

class ScannedDocument(var original: Mat?) {

    var processed: Mat? = null
    var quadrilateral: Quadrilateral? = null
    var previewPoints: ArrayList<Point>? = null
    var previewSize: Size? = null


    fun setProcessed(processed: Mat): ScannedDocument {
        this.processed = processed
        return this
    }

    fun release() {
        if (processed != null) {
            processed!!.release()
        }
        if (original != null) {
            original!!.release()
        }

        quadrilateral?.contour?.let {
            it.release()
        }
    }
}
package com.wachirapong.kdocscan.util

import android.graphics.*
import androidx.core.content.ContextCompat
import com.wachirapong.kdocscan.data.Quadrilateral
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.abs

private const val THRESHOLD_1 = 20.0
private const val THRESHOLD_2 = 50.0

class ImageProcessor {

    private var ratio = 0.0
    private var size: Size? = null
    private var lastTransformDocument: Mat? = null
    private var countSimilarDocument = 0

    private var originalWidth = 0
    private var originalHeight = 0

    fun findDocument(image: Bitmap): Quadrilateral? {
        val originalMat = Mat(Size(image.width.toDouble(), image.height.toDouble()), CvType.CV_8UC4)
        image.toMat(originalMat)

        // Resize Image
        ratio = image.height.toDouble() / 500.0
        originalWidth = image.width
        originalHeight = image.height
        val width = image.width.toDouble() / ratio
        val height= image.height.toDouble() / ratio
        size = Size(width, height)
        val resizedImage = Mat(size, CvType.CV_8UC4)
        Imgproc.resize(originalMat, resizedImage, size)

        // Detect Edge
        val edged = Mat(size, CvType.CV_8UC1)
        detectEdge(resizedImage, edged)

        // Find Contour
        val contour: ArrayList<MatOfPoint> = findContour(edged)

        // Find Quadrilateral
        return getQuadrilateral(contour)
    }

    fun convertToPreviewPoint(quadrilateral: Quadrilateral?, width: Int, height: Int): Quadrilateral? {
        return quadrilateral?.let {
            it.points?.forEach { point ->
                point.x *= ratio * (width / originalWidth)
                point.y *= ratio * (height / originalHeight)
            }
            it
        }
    }

    fun Quadrilateral.convertQuadrilateralToOriginalSize(): Quadrilateral {
        this.points?.forEach { point ->
            point.x *= ratio
            point.y *= ratio
        }
        return this
    }

    fun drawDocumentBox(
        image: Bitmap,
        quadrilateral: Quadrilateral?
    ): Bitmap {
        quadrilateral?.points?.let { points ->
            quadrilateral.convertQuadrilateralToOriginalSize()
            val path = Path().apply {
                moveTo(points[0].x.toFloat() , points[0].y.toFloat())
                lineTo(points[1].x.toFloat(), points[1].y.toFloat())
                lineTo(points[2].x.toFloat(), points[2].y.toFloat())
                lineTo(points[3].x.toFloat(), points[3].y.toFloat())
                close()
            }
            val paint = Paint().apply {
                color = Color.parseColor("#ff99cc00")
                style = Paint.Style.STROKE
                strokeWidth = 5f // TODO: should take from resources
            }
            val canvas = Canvas(image)
            canvas.drawPath(path, paint)
        }
        return image
    }

    private fun detectEdge(picture: Mat, edged: Mat) {
        // convert the image to grayscale, blur it, and find edges
        // in the image
        val grayScale = Mat(size, CvType.CV_8UC4)
        findCannyEdge(picture, grayScale, edged)
        grayScale.release()
//        findThreshold(picture, grayScale, edged)
    }

    private fun findCannyEdge(picture: Mat, grayScale: Mat, edged: Mat) {
        Imgproc.cvtColor(picture, grayScale, Imgproc.COLOR_BGR2GRAY, 4)
        Imgproc.GaussianBlur(grayScale, grayScale, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(grayScale, edged, THRESHOLD_1, THRESHOLD_2)
        Imgproc.adaptiveThreshold(edged, edged, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)
    }

    private fun findThreshold(picture: Mat, grayScale: Mat, edged: Mat) {
        Imgproc.cvtColor(picture, grayScale, Imgproc.COLOR_BGRA2GRAY)
//        Imgproc.GaussianBlur(grayScale, grayScale, Size(5.0, 5.0), 0.0)
//        Imgproc.threshold(grayScale, edged, threshold1, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)
        Imgproc.adaptiveThreshold(grayScale, edged, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 4.0)
    }

    private fun findContour(edged: Mat): ArrayList<MatOfPoint> {
        // # find the contours in the edged image, keeping only the
        // # largest ones, and initialize the screen contour
        val copyEdged = Mat(size, CvType.CV_8UC4)
        edged.copyTo(copyEdged)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(copyEdged, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()
        contours.sortWith(Comparator { lhs: MatOfPoint, rhs: MatOfPoint ->
            Imgproc.contourArea(rhs).compareTo(Imgproc.contourArea(lhs))
        })
        copyEdged.release()
        return contours
    }

    private fun getQuadrilateral(contours: ArrayList<MatOfPoint>): Quadrilateral? {
        // loop over the contours
        var biggest: Quadrilateral? = null
        var maxArea = 0.0
        for (c in contours) {
            val area = abs(Imgproc.contourArea(c))
            // filter area less than 100
            if(area < 1000) continue
            // approximate the contour
            val c2f = MatOfPoint2f()
            c.convertTo(c2f, CvType.CV_32FC2)
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)

            val points = approx.toList()

            // if our approximated contour has four points, then we
            // can assume that we have found our screen
            // select biggest 4 angles polygon
            if (points.size == 4 && area > maxArea) {
                val foundPoints = sortPoints(points)
                biggest = Quadrilateral(c, foundPoints)
                maxArea = area
            }
        }
        return biggest
    }

    private fun sortPoints(src: List<Point>): List<Point> {

        val srcPoints = ArrayList(src)

        val result = arrayListOf<Point>()

        val sumComparator = Comparator { lhs: Point, rhs: Point ->
            (lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
        }

        val diffComparator = Comparator { lhs: Point, rhs: Point ->
            (lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
        }

        // top-left corner = minimal sum
        result.add(0, Collections.min(srcPoints, sumComparator))

        // bottom-left corner = maximal diference
        result.add(1, Collections.max(srcPoints, diffComparator))

        // bottom-right corner = maximal sum
        result.add(2, Collections.max(srcPoints, sumComparator))

        // top-right corner = minimal diference
        result.add(3, Collections.min(srcPoints, diffComparator))
        return result
    }

    private fun isSameDocument(document: Mat): Boolean {
        val copyDoc = Mat(document.size(), document.type())
        if (lastTransformDocument != null) {
            val lastDocumentWidth = lastTransformDocument?.width() ?: 0
            val lastDocumentHeight = lastTransformDocument?.height() ?: 0
            val widthMeasure = lastDocumentWidth / 10
            val heightMeasure = lastDocumentHeight / 10

            val newDocumentWidth = copyDoc.width()
            val newDocumentHeight = copyDoc.height()

            val widthDiff = abs(lastDocumentWidth - newDocumentWidth)
            val heightDiff = abs(lastDocumentHeight - newDocumentHeight)
            if (widthDiff <= widthMeasure && heightDiff <= heightMeasure) {
                countSimilarDocument++
            } else {
                countSimilarDocument = 0
            }
        }
        lastTransformDocument = Mat(copyDoc.size(), copyDoc.type())
        copyDoc.copyTo(lastTransformDocument)
        copyDoc.release()
        return countSimilarDocument >= 10
    }
}
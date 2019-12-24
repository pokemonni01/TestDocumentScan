package com.wachirapong.kdocscan.util

import android.graphics.*
import com.wachirapong.kdocscan.data.Quadrilateral
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.abs

private const val THRESHOLD_1 = 0.0
private const val THRESHOLD_2 = 30.0

class ImageProcessor {

    var ratio = 0.0
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

    fun convertQuadrilateralToOriginalSize(quadrilateral: Quadrilateral): Quadrilateral {
        quadrilateral.points?.forEach { point ->
            point.x *= ratio
            point.y *= ratio
        }
        return quadrilateral
    }

    fun drawDocumentBox(
        image: Bitmap,
        quadrilateral: Quadrilateral?
    ): Bitmap {
        quadrilateral?.points?.let { points ->
            convertQuadrilateralToOriginalSize(quadrilateral)
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
                isAntiAlias = true
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

    private var lastDocumentArea = 0.0

    fun isSameDocument(document: Quadrilateral?): Boolean {
        val area = calculateArea(document?.points)
        val areMeasure = lastDocumentArea / 10
        val areaDiff = abs(lastDocumentArea - area)
        lastDocumentArea = area
        if (areMeasure > 0 && areaDiff <= areMeasure) {
            countSimilarDocument++
        } else {
            countSimilarDocument = 0
        }
        return countSimilarDocument >= 10
    }

    private fun calculateArea(points: List<Point>?): Double {
        if (points?.size == 4) {
            val tl = points[0]
            val tr = points[3]
            val br = points[2]
            val bl = points[1]
            return abs(
                ((tl.x * tr.y - tl.y * tr.x) + (tr.x * br.y - tr.y * br.x) +
                        (br.x * bl.y - br.y * bl.x) + (bl.x * tl.y - bl.y * tl.x)) / 2)
        }
        return 0.0
    }
}
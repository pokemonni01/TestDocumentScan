//package com.wachirapong.kdocscan.util
//
//import android.content.ContentValues.TAG
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Path
//import android.graphics.drawable.shapes.PathShape
//import android.util.Log
//import com.wachirapong.kdocscan.data.Quadrilateral
//import com.wachirapong.kdocscan.data.ScannedDocument
//import org.opencv.core.*
//import org.opencv.imgcodecs.Imgcodecs
//import org.opencv.imgproc.Imgproc
//import java.util.*
//import kotlin.collections.ArrayList
//import kotlin.experimental.and
//import kotlin.math.max
//
//
//class ImageProcessor {
//    private var mBugRotate: Boolean = false
//    private var colorMode = false
//    private var filterMode = true
//    private val colorGain = 1.5       // contrast
//    private val colorBias = 0.0         // bright
//    private val colorThresh = 110        // threshold
//    private var mPreviewSize: Size? = null
//    private var mPreviewPoints: ArrayList<Point>? = null
//    private var qrResultPoints: Array<ResultPoint>? = null
//
//
//    fun processPicture(picture: Mat) {
//
//        val img = Imgcodecs.imdecode(picture, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
//        picture.release()
//
//        Log.d(TAG, "processPicture - imported image " + img.size().width + "x" + img.size().height)
//
//        if (mBugRotate) {
//            Core.flip(img, img, 1)
//            Core.flip(img, img, 0)
//        }
//
//        val doc = detectDocument(img)
//        mMainActivity.saveDocument(doc)
//
//        doc.release()
//        picture.release()
//
//        mMainActivity.setImageProcessorBusy(false)
//        mMainActivity.waitSpinnerInvisible()
//    }
//
//
//    private fun detectDocument(inputRgba: Mat): ScannedDocument {
//        val contours = findContours(inputRgba)
//
//        val sd = ScannedDocument(inputRgba)
//
//        val quad = getQuadrilateral(contours, inputRgba.size())
//
//        val doc: Mat
//
//        if (quad != null) {
//
//            val c = quad.contour
//
//            sd.quadrilateral = quad
//            sd.previewPoints = mPreviewPoints
//            sd.previewSize = mPreviewSize
//
//            doc = fourPointTransform(inputRgba, quad.points!!)
//
//        } else {
//            doc = Mat(inputRgba.size(), CvType.CV_8UC4)
//            inputRgba.copyTo(doc)
//        }
//
//        enhanceDocument(doc)
//        return sd.setProcessed(doc)
//    }
//
//    private fun detectPreviewDocument(inputRgba: Mat): Boolean {
//
//        val contours = findContours(inputRgba)
//
//        val quad = getQuadrilateral(contours, inputRgba.size())
//
//        mPreviewPoints = null
//        mPreviewSize = inputRgba.size()
//
//        if (quad != null) {
//
//            val rescaledPoints = ArrayList<Point>(4)
//
//            val ratio = inputRgba.size().height / 500
//
//            for (i in 0..3) {
//                val x = java.lang.Double.valueOf(quad.points?.get(i)?.x!! * ratio).toInt()
//                val y = java.lang.Double.valueOf(quad.points?.get(i)?.y!! * ratio).toInt()
//                if (mBugRotate) {
//                    rescaledPoints[(i + 2) % 4] = Point(
//                        Math.abs(x - mPreviewSize!!.width),
//                        Math.abs(y - mPreviewSize!!.height)
//                    )
//                } else {
//                    rescaledPoints[i] = Point(x.toDouble(), y.toDouble())
//                }
//            }
//
//            mPreviewPoints = rescaledPoints
//
//            drawDocumentBox(mPreviewPoints!!, mPreviewSize!!)
//
//            Log.d(
//                TAG,
//                quad!!.points!![0].toString() + " , " + quad!!.points!![1].toString() + " , " + quad!!.points!![2].toString() + " , " + quad!!.points!![3].toString()
//            )
//
//            return true
//        }
//
//        mMainActivity.getHUD().clear()
//        mMainActivity.invalidateHUD()
//
//        return false
//    }
//
//    private fun drawDocumentBox(points: ArrayList<Point>, stdSize: Size) {
//
//        val path = Path()
//
//        val hud = mMainActivity.getHUD()
//
//        // ATTENTION: axis are swapped
//
//        val previewWidth = stdSize.height as Float
//        val previewHeight = stdSize.width as Float
//
//        path.moveTo(previewWidth - points[0].y as Float, points[0].x as Float)
//        path.lineTo(previewWidth - points[1].y as Float, points[1].x as Float)
//        path.lineTo(previewWidth - points[2].y as Float, points[2].x as Float)
//        path.lineTo(previewWidth - points[3].y as Float, points[3].x as Float)
//        path.close()
//
//        val newBox = PathShape(path, previewWidth, previewHeight)
//
//        val paint = Paint()
//        paint.setColor(Color.argb(64, 0, 255, 0))
//
//        val border = Paint()
//        border.setColor(Color.rgb(0, 255, 0))
//        border.setStrokeWidth(5f)
//
//        hud.clear()
//        hud.addShape(newBox, paint, border)
//        mMainActivity.invalidateHUD()
//    }
//
//    private fun getQuadrilateral(contours: ArrayList<MatOfPoint>, srcSize: Size): Quadrilateral? {
//
//        val ratio = srcSize.height / 500
//        val height = java.lang.Double.valueOf(srcSize.height / ratio).toInt()
//        val width = java.lang.Double.valueOf(srcSize.width / ratio).toInt()
//        val size = Size(width.toDouble(), height.toDouble())
//
//        for (c in contours) {
//            val c2f = MatOfPoint2f(c.toArray())
//            val peri = Imgproc.arcLength(c2f, true)
//            val approx = MatOfPoint2f()
//            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
//
//            val points = approx.toArray()
//
//            // select biggest 4 angles polygon
//            if (points.size == 4) {
//                val foundPoints = sortPoints(points)
//
//                if (insideArea(foundPoints, size)) {
//                    return Quadrilateral(c, foundPoints)
//                }
//            }
//        }
//
//        return null
//    }
//
//    private fun sortPoints(src: ArrayList<Point>): ArrayList<Point> {
//
//        val srcPoints = ArrayList(Arrays.asList(src))
//
//        val result = arrayOf(null, null, null, null)
//
//        val sumComparator = object : Comparator<Point>() {
//            fun compare(lhs: Point, rhs: Point): Int {
//                return java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
//            }
//        }
//
//        val diffComparator = object : Comparator<Point>() {
//
//            fun compare(lhs: Point, rhs: Point): Int {
//                return java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
//            }
//        }
//
//        // top-left corner = minimal sum
//        result[0] = Collections.min(srcPoints, sumComparator)
//
//        // bottom-right corner = maximal sum
//        result[2] = Collections.max(srcPoints, sumComparator)
//
//        // top-right corner = minimal diference
//        result[1] = Collections.min(srcPoints, diffComparator)
//
//        // bottom-left corner = maximal diference
//        result[3] = Collections.max(srcPoints, diffComparator)
//
//        return result
//    }
//
//    private fun insideArea(rp: Array<Point>, size: Size): Boolean {
//
//        val width = java.lang.Double.valueOf(size.width).toInt()
//        val height = java.lang.Double.valueOf(size.height).toInt()
//        val baseMeasure = height / 4
//
//        val bottomPos = height - baseMeasure
//        val leftPos = width / 2 - baseMeasure
//        val rightPos = width / 2 + baseMeasure
//
//        return (rp[0].x <= leftPos && rp[0].y <= baseMeasure
//                && rp[1].x >= rightPos && rp[1].y <= baseMeasure
//                && rp[2].x >= rightPos && rp[2].y >= bottomPos
//                && rp[3].x <= leftPos && rp[3].y >= bottomPos)
//    }
//
//    private fun enhanceDocument(src: Mat) {
//        if (colorMode && filterMode) {
//            src.convertTo(src, -1, colorGain, colorBias)
//            val mask = Mat(src.size(), CvType.CV_8UC1)
//            Imgproc.cvtColor(src, mask, Imgproc.COLOR_RGBA2GRAY)
//
//            val copy = Mat(src.size(), CvType.CV_8UC3)
//            src.copyTo(copy)
//
//            Imgproc.adaptiveThreshold(
//                mask,
//                mask,
//                255,
//                Imgproc.ADAPTIVE_THRESH_MEAN_C,
//                Imgproc.THRESH_BINARY_INV,
//                15,
//                15
//            )
//
//            src.setTo(Scalar(255, 255, 255))
//            copy.copyTo(src, mask)
//
//            copy.release()
//            mask.release()
//
//            // special color threshold algorithm
//            colorThresh(src, colorThresh)
//        } else if (!colorMode) {
//            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY)
//            if (filterMode) {
//                Imgproc.adaptiveThreshold(
//                    src,
//                    src,
//                    255,
//                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
//                    Imgproc.THRESH_BINARY,
//                    15,
//                    15
//                )
//            }
//        }
//    }
//
//    /**
//     * When a pixel have any of its three elements above the threshold
//     * value and the average of the three values are less than 80% of the
//     * higher one, brings all three values to the max possible keeping
//     * the relation between them, any absolute white keeps the value, all
//     * others go to absolute black.
//     *
//     * src must be a 3 channel image with 8 bits per channel
//     *
//     * @param src
//     * @param threshold
//     */
//    private fun colorThresh(src: Mat, threshold: Int) {
//        val srcSize = src.size()
//        val size = (srcSize.height * srcSize.width) as Int * 3
//        val d = ByteArray(size)
//        src.get(0, 0, d)
//
//        var i = 0
//        while (i < size) {
//
//            // the "& 0xff" operations are needed to convert the signed byte to double
//
//            // avoid unneeded work
//            if ((d[i] and 0xff.toByte()).toDouble() == 255.0) {
//                i += 3
//                continue
//            }
//
//            val max = Math.max(
//                max((d[i] and 0xff.toByte()).toDouble(), (d[i + 1] and 0xff.toByte()).toDouble()),
//                (d[i + 2] and 0xff.toByte()).toDouble()
//            )
//            val mean = ((d[i] and 0xff.toByte()).toDouble() + (d[i + 1] and 0xff.toByte()).toDouble()
//                    + (d[i + 2] and 0xff.toByte()).toDouble()) / 3
//
//            if (max > threshold && mean < max * 0.8) {
//                d[i] = ((d[i] and 0xff.toByte()).toDouble() * 255 / max).toByte()
//                d[i + 1] = ((d[i + 1] and 0xff.toByte()).toDouble() * 255 / max).toByte()
//                d[i + 2] = ((d[i + 2] and 0xff.toByte()).toDouble() * 255 / max).toByte()
//            } else {
//                d[i + 2] = 0
//                d[i + 1] = d[i + 2]
//                d[i] = d[i + 1]
//            }
//            i += 3
//        }
//        src.put(0, 0, d)
//    }
//
//    private fun fourPointTransform(src: Mat, pts: ArrayList<Point>): Mat {
//
//        val ratio = src.size().height / 500
//        val height = java.lang.Double.valueOf(src.size().height / ratio).toInt()
//        val width = java.lang.Double.valueOf(src.size().width / ratio).toInt()
//
//        val tl = pts[0]
//        val tr = pts[1]
//        val br = pts[2]
//        val bl = pts[3]
//
//        val widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0) + Math.pow(br.y - bl.y, 2.0))
//        val widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0) + Math.pow(tr.y - tl.y, 2.0))
//
//        val dw = Math.max(widthA, widthB) * ratio
//        val maxWidth = java.lang.Double.valueOf(dw).toInt()
//
//
//        val heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0) + Math.pow(tr.y - br.y, 2.0))
//        val heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0) + Math.pow(tl.y - bl.y, 2.0))
//
//        val dh = Math.max(heightA, heightB) * ratio
//        val maxHeight = java.lang.Double.valueOf(dh).toInt()
//
//        val doc = Mat(maxHeight, maxWidth, CvType.CV_8UC4)
//
//        val src_mat = Mat(4, 1, CvType.CV_32FC2)
//        val dst_mat = Mat(4, 1, CvType.CV_32FC2)
//
//        src_mat.put(
//            0,
//            0,
//            tl.x * ratio,
//            tl.y * ratio,
//            tr.x * ratio,
//            tr.y * ratio,
//            br.x * ratio,
//            br.y * ratio,
//            bl.x * ratio,
//            bl.y * ratio
//        )
//        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh)
//
//        val m = Imgproc.getPerspectiveTransform(src_mat, dst_mat)
//
//        Imgproc.warpPerspective(src, doc, m, doc.size())
//
//        return doc
//    }
//
//    private fun findContours(src: Mat): ArrayList<MatOfPoint> {
//
//        var grayImage: Mat? = null
//        var cannedImage: Mat? = null
//        var resizedImage: Mat? = null
//
//        val ratio = src.size().height / 500
//        val height = java.lang.Double.valueOf(src.size().height / ratio).toInt()
//        val width = java.lang.Double.valueOf(src.size().width / ratio).toInt()
//        val size = Size(width, height)
//
//        resizedImage = Mat(size, CvType.CV_8UC4)
//        grayImage = Mat(size, CvType.CV_8UC4)
//        cannedImage = Mat(size, CvType.CV_8UC1)
//
//        Imgproc.resize(src, resizedImage, size)
//        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4)
//        Imgproc.GaussianBlur(grayImage, grayImage, Size(5, 5), 0)
//        Imgproc.Canny(grayImage, cannedImage, 75, 200)
//
//        val contours = ArrayList<MatOfPoint>()
//        val hierarchy = Mat()
//
//        Imgproc.findContours(
//            cannedImage,
//            contours,
//            hierarchy,
//            Imgproc.RETR_LIST,
//            Imgproc.CHAIN_APPROX_SIMPLE
//        )
//
//        hierarchy.release()
//
//        Collections.sort(contours, object : Comparator<MatOfPoint>() {
//
//            fun compare(lhs: MatOfPoint, rhs: MatOfPoint): Int {
//                return java.lang.Double.valueOf(Imgproc.contourArea(rhs))
//                    .compareTo(Imgproc.contourArea(lhs))
//            }
//        })
//
//        resizedImage!!.release()
//        grayImage!!.release()
//        cannedImage!!.release()
//
//        return contours
//    }
//}
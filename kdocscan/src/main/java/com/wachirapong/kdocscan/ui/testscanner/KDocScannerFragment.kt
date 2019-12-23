package com.wachirapong.kdocscan.ui.testscanner


import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.data.Quadrilateral
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.util.ImageUtil
import com.wachirapong.kdocscan.util.toBitMap
import com.wachirapong.kdocscan.util.toMat
import kotlinx.android.synthetic.main.fragment_kdoc_scanner.*
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


class KDocScannerFragment : BaseFragment() {

    companion object {
        fun initInstance() = KDocScannerFragment()
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var threshold1 = 50.0
    private var threshold2 = 80.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kdoc_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekThreshold1.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{

            var progressChangedValue = 0

            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                progressChangedValue = progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                threshold1 = progressChangedValue.toDouble()
                tvThreshold1Value.text = threshold1.toString()
            }

        })
        seekThreshold2.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{

            var progressChangedValue = 0

            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                progressChangedValue = progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                threshold2 = progressChangedValue.toDouble()
                tvThreshold2Value.text = threshold2.toString()
            }

        })
    }

    override fun onOpenCVConnected() {
        startCamera()
    }

    override fun onOpenCVConnectionFailed() {

    }

    override fun onAfterViewCreated() {

    }

    fun startCamera() {
        context?.let {
            cameraProviderFuture = ProcessCameraProvider.getInstance(it)
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(LensFacing.BACK).build()
                val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
                preview.previewSurfaceProvider = previewView.previewSurfaceProvider
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, getImageAnalysis(it))
            }, ContextCompat.getMainExecutor(it))
        }
    }

    var ratio = 0.0
    private var size: Size? = null

    private fun getImageAnalysis(context: Context): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.BackpressureStrategy.KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context),
                    ImageAnalysis.Analyzer { image, rotationDegrees ->
                        val original = ImageUtil.imageToBitmap(image, rotationDegrees.toFloat())
                        val originalMat = Mat(Size(original.width.toDouble(), original.height.toDouble()), CvType.CV_8UC4)
                        original.toMat(originalMat)

                        // bitmap to mat
                        ratio = original.height.toDouble() / 500.0
                        val width = original.width.toDouble() / ratio
                        val height= original.height.toDouble() / ratio
                        size = Size(width, height)
                        val resizedImage = Mat(size, CvType.CV_8UC4)
                        Imgproc.resize(originalMat, resizedImage, size)

                        // Find EDGE
                        val grayScale = Mat(size, CvType.CV_8UC4)
                        val edged = Mat(size, CvType.CV_8UC1)
                        edgeDetection(resizedImage, grayScale, edged)

                        // Find Contour
                        val contour: ArrayList<MatOfPoint> = findContour(edged)

                        // Find Quadrilateral
                        val quadrilateral = getQuadrilateral(contour)

                        val preview = Bitmap.createBitmap(resizedImage.cols(), resizedImage.rows(), Bitmap.Config.ARGB_8888)
                        resizedImage.toBitMap(preview)
                        var croppedBitmap: Bitmap? = null
                        if (quadrilateral != null) {
                            drawDocumentBox(quadrilateral.points, preview)

                            val fpt = fourPointTransform(originalMat, quadrilateral.points!!)
                            if (isSameDocument(fpt)) {
                                croppedBitmap = Bitmap.createBitmap(
                                    fpt.cols(),
                                    fpt.rows(), Bitmap.Config.ARGB_8888)
                                fpt.toBitMap(croppedBitmap)
//                                saveMatToFile(fpt)
                            }
                        }
                        // END
                        (context as Activity).runOnUiThread {
                            ivPreView.setImageBitmap(original)
                            val previewEdge = Bitmap.createBitmap(resizedImage.cols(), resizedImage.rows(), Bitmap.Config.ARGB_8888)
                            edged.toBitMap(previewEdge)
                            ivPreViewEdge.setImageBitmap(previewEdge)
                            ivPreViewDetect.setImageBitmap(preview)
                            croppedBitmap?.let {
                                ivCrop.setImageBitmap(it)
                            }
                        }
                        image.close()
                    }
                )
            }
    }

    private fun edgeDetection(picture: Mat, grayScale: Mat, edged: Mat) {
        // convert the image to grayscale, blur it, and find edges
        // in the image
        findCannyEdge(picture, grayScale, edged)
//        findThreshold(picture, grayScale, edged)
    }

    private fun findCannyEdge(picture: Mat, grayScale: Mat, edged: Mat) {
        Imgproc.cvtColor(picture, grayScale, Imgproc.COLOR_BGR2GRAY, 4)
        Imgproc.GaussianBlur(grayScale, grayScale, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(grayScale, edged, threshold1, threshold2)
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

    private fun insideArea(rp: List<Point>): Boolean {

        val width = size?.width ?: 0.0
        val height = size?.height ?: 0.0
        val baseMeasure = height / 4.0

        val bottomPos = height - baseMeasure
        val topPos = baseMeasure
        val leftPos = width / 2 - baseMeasure
        val rightPos = width / 2 + baseMeasure

        return (rp[0].x <= leftPos && rp[0].y <= topPos
                && rp[3].x >= rightPos && rp[3].y <= topPos
                && rp[2].x >= rightPos && rp[2].y >= bottomPos
                && rp[1].x <= leftPos && rp[1].y >= bottomPos)
    }

    private fun drawDocumentBox(
        points: List<Point>?,
        image: Bitmap
    ) {
        if (points == null) return
        val path = Path().apply {
            moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            lineTo(points[1].x.toFloat(), points[1].y.toFloat())
            lineTo(points[2].x.toFloat(), points[2].y.toFloat())
            lineTo(points[3].x.toFloat(), points[3].y.toFloat())
            close()
        }
        val paint = Paint()
        paint.color = Color.RED
        val canvas = Canvas(image)
        canvas.drawPath(path, paint)
    }

    private fun fourPointTransform(src: Mat, pts: List<Point>): Mat {
        val tl = pts[0]
        val tr = pts[3]
        val br = pts[2]
        val bl = pts[1]

        val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
        val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))

        val dw = max(widthA, widthB) * ratio
        val maxWidth = dw.toInt()


        val heightA = sqrt((tr.x - br.x).pow(2.0) + (tr.y - br.y).pow(2.0))
        val heightB = sqrt((tl.x - bl.x).pow(2.0) + (tl.y - bl.y).pow(2.0))

        val dh = max(heightA, heightB) * ratio
        val maxHeight = dh.toInt()

        val doc = Mat(maxHeight, maxWidth, CvType.CV_8UC4)

        val srcMat = Mat(4, 1, CvType.CV_32FC2)
        val dstMat = Mat(4, 1, CvType.CV_32FC2)

        srcMat.put(
            0,
            0,
            tl.x * ratio,
            tl.y * ratio,
            tr.x * ratio,
            tr.y * ratio,
            br.x * ratio,
            br.y * ratio,
            bl.x * ratio,
            bl.y * ratio
        )
        dstMat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh)

        val m = Imgproc.getPerspectiveTransform(srcMat, dstMat)

        Imgproc.warpPerspective(src, doc, m, doc.size())

        return doc
    }

    private var lastTransformDocument: Mat? = null
    private var countSimilarDocument = 0

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

//    private fun saveMatToFile(documentImage: Mat) {
//        context?.let {
//            val docBitmap = Bitmap.createBitmap(documentImage.cols(),
//                documentImage.rows(), Bitmap.Config.ARGB_8888)
//            val fileName = "scanned_document"
//            val destination = File(it.filesDir, fileName)
//            val values = ContentValues()
//            values.put(MediaStore.Images.Media.TITLE, fileName)
//            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//            val uri = it.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
//
//            it.openFileOutput(fileName, Context.MODE_PRIVATE).use { fileOutputStream ->
//                fileOutputStream.write(docBitmap.)
//            }
//            it.openFileInput()
//        }
//    }

//    public fun getImageUri(ctx: Context , bitmap: Bitmap): Uri {
//        val bytes = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path = MediaStore.Images.ImageColumns.IS_PENDING
//        return Uri.parse(path)
//    }
}


package com.wachirapong.kdocscan.ui.scanner


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.data.Quadrilateral
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.util.ImageUtil
import kotlinx.android.synthetic.main.fragment_kdoc_scanner.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import android.graphics.*
import com.wachirapong.kdocscan.util.toBitMap
import com.wachirapong.kdocscan.util.toMat
import org.opencv.core.Point


class KDocScannerFragment : BaseFragment() {

    companion object {
        fun initInstance() = KDocScannerFragment()
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kdoc_scanner, container, false)
    }

    override fun onOpenCVConnected() {
        startCamera()
    }

    override fun onOpenCVConnectionFailed() {

    }

    override fun onAfterViewCreated() {

    }

    private fun startCamera() {
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

    private var size: Size? = null

    private fun getImageAnalysis(context: Context): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.BackpressureStrategy.BLOCK_PRODUCER)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context),
                    ImageAnalysis.Analyzer { image, rotationDegrees ->
                        val original = ImageUtil.imageToBitmap(image.image!!, rotationDegrees.toFloat())

                        // bitmap to mat
                        val ratio = original.height.toDouble() / 500.0
                        val width = image.width.toDouble() / ratio
                        val height= image.height.toDouble() / ratio
                        size = Size(width, height)
                        val originalMat = Mat(size, CvType.CV_8UC4)
                        val preview = original.copy(Bitmap.Config.ARGB_8888, true)
                        preview.toMat(originalMat)

                        // Find EDGE
                        val grayScale = Mat(size, CvType.CV_8UC4)
                        val edged = Mat(size, CvType.CV_8UC1)
                        edgeDetection(originalMat, grayScale, edged)

                        // Find Contour
                        val contour: ArrayList<MatOfPoint> = findContour(edged)

                        // Find Quadrilateral
                        val quadrilateral = getQuadrilateral(contour)

                        val previewImage = Mat(size, CvType.CV_8UC4)
                        originalMat.copyTo(previewImage)
                        Utils.matToBitmap(previewImage, preview)
                        if (quadrilateral != null) {
                            drawDocumentBox(quadrilateral.points, preview)
                        }
                        // END
                        (context as Activity).runOnUiThread {
                            // FOR TEST
                            edged.toBitMap(preview)

                            imageView.setImageBitmap(preview)
                        }
                        image.close()
                    }
                )
            }
    }

    private fun edgeDetection(picture: Mat, grayScale: Mat, edged: Mat) {
        // convert the image to grayscale, blur it, and find edges
        // in the image
        Imgproc.cvtColor(picture, grayScale, Imgproc.COLOR_RGBA2GRAY, 4)
        Imgproc.GaussianBlur(grayScale, grayScale, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(picture, edged, 75.0, 200.0)
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
        for (c in contours) {
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
            if (points.size == 4) {

                val foundPoints = sortPoints(points)


                if (insideArea(foundPoints)) {
                    return Quadrilateral(c, foundPoints)
                }
            }
        }
        return null
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

        // top-right corner = minimal diference
        result.add(1, Collections.min(srcPoints, diffComparator))

        // bottom-right corner = maximal sum
        result.add(2, Collections.max(srcPoints, sumComparator))

        // bottom-left corner = maximal diference
        result.add(3, Collections.max(srcPoints, diffComparator))

        return result
    }

    private fun insideArea(rp: List<Point>): Boolean {

        val width = size?.width ?: 0.0
        val height = size?.width ?: 0.0
        val baseMeasure = height / 4.0

        val bottomPos = height - baseMeasure
        val leftPos = width / 2 - baseMeasure
        val rightPos = width / 2 + baseMeasure

        return (rp[0].x <= leftPos && rp[0].y <= baseMeasure
                && rp[1].x >= rightPos && rp[1].y <= baseMeasure
                && rp[2].x >= rightPos && rp[2].y >= bottomPos
                && rp[3].x <= leftPos && rp[3].y >= bottomPos)
    }

    private fun drawDocumentBox(
        points: List<Point>?,
        image: Bitmap
    ) {
        if (points == null) return
        val path = Path().apply {
            moveTo(points[0].y.toFloat(), points[0].x.toFloat())
            lineTo(points[1].y.toFloat(), points[1].x.toFloat())
            lineTo(points[2].y.toFloat(), points[2].x.toFloat())
            lineTo(points[3].y.toFloat(), points[3].x.toFloat())
            close()
        }
        val paint = Paint()
        paint.color = Color.RED
        val canvas = Canvas(image)
        canvas.drawPath(path, paint)
    }
}


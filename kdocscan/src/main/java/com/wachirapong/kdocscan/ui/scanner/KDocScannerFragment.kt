package com.wachirapong.kdocscan.ui.scanner


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
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
                        size = Size(image.width.toDouble(), image.height.toDouble())
                        val originalMat = Mat(size, CvType.CV_8UC4)
                        val bmp32 = original.copy(Bitmap.Config.ARGB_8888, true)
                        Utils.bitmapToMat(bmp32, originalMat)
                        val grayScale = Mat(size, CvType.CV_8UC4)
                        val gaussianBlur = Mat(size, CvType.CV_8UC4)

                        // convert the image to grayscale, blur it, and find edges
                        // in the image
                        val edged = Mat(size, CvType.CV_8UC1)
                        Imgproc.cvtColor(originalMat, grayScale, Imgproc.COLOR_RGBA2GRAY)
                        Imgproc.GaussianBlur(grayScale, gaussianBlur, Size(5.0, 5.0), 0.0)
                        Imgproc.Canny(originalMat, edged, 75.0, 200.0)
                        Utils.matToBitmap(edged, bmp32)

                        val contour: ArrayList<MatOfPoint> = findContour(edged)

                        val a = getQuadrilateral(contour)

                        val imageWithContour = Mat(size, CvType.CV_8UC4)
                        originalMat.copyTo(imageWithContour)
                        if (a != null) {
                            Imgproc.drawContours(imageWithContour, contour, -1, Scalar(0.0, 255.0, 0.0), 2)
                        }
                        Utils.matToBitmap(imageWithContour, bmp32)
                        // END
                        (context as Activity).runOnUiThread {
                            imageView.setImageBitmap(bmp32)
                        }
                        image.close()
                    }
                )
            }
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

    private fun getQuadrilateral(contours: ArrayList<MatOfPoint>): Mat? {
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
//
//                if (insideArea(foundPoints, size)) {
//                    return Quadrilateral(c, foundPoints)
//                }
                return approx
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
        result[0] = Collections.min(srcPoints, sumComparator)

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator)

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator)

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator)

        return result
    }
}


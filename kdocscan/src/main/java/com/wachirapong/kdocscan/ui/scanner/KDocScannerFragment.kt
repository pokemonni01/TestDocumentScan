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
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.util.ImageUtil
import kotlinx.android.synthetic.main.fragment_kdoc_scanner.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


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

    private fun getImageAnalysis(context: Context): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.BackpressureStrategy.BLOCK_PRODUCER)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context),
                    ImageAnalysis.Analyzer { image, rotationDegrees ->
                        val original = ImageUtil.imageToBitmap(image.image!!, rotationDegrees.toFloat())

                        // bitmap to mat
                        val size = Size(image.width.toDouble(), image.height.toDouble())
                        val originalMat = Mat(size, CvType.CV_8UC4)
                        val bmp32 = original.copy(Bitmap.Config.ARGB_8888, true)
                        Utils.bitmapToMat(bmp32, originalMat)

                        // convert the image to grayscale, blur it, and find edges
                        // in the image
                        val edged = Mat(size, CvType.CV_8UC1)
                        Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_RGBA2GRAY)
                        Imgproc.GaussianBlur(originalMat, originalMat, Size(5.0, 5.0), 0.0)
                        Imgproc.Canny(originalMat, edged, 75.0, 200.0)
                        Utils.matToBitmap(edged, bmp32)
                        findContour()
                        // END
                        (context as Activity).runOnUiThread {
                            imageView.setImageBitmap(bmp32)
                        }
                        image.close()
                    }
                )
            }
    }

    private fun findContour(edged: Mat) {
        Imgproc.findContours()
    }

}


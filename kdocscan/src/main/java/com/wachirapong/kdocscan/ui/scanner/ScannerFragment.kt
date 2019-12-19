package com.wachirapong.kdocscan.ui.scanner


import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
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
import com.wachirapong.kdocscan.util.ImageProcessor
import com.wachirapong.kdocscan.util.ImageUtil
import kotlinx.android.synthetic.main.fragment_scanner.*
import org.koin.android.ext.android.inject

class ScannerFragment : BaseFragment(), ScannerContract.View {

    companion object {
        fun initInstance() = ScannerFragment()
    }

    private val presenter: ScannerContract.Presenter by inject()
    private val imageProcessor: ImageProcessor by inject()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.provideView(this)
        initView()
    }

    override fun onOpenCVConnected() {
        startCamera()
    }

    override fun onOpenCVConnectionFailed() {

    }

    override fun onAfterViewCreated() {

    }

    override fun showAutoScan() {
        tvAutoScan?.text = getString(R.string.auto_scan)
    }

    override fun showManualScan() {
        tvAutoScan?.text = getString(R.string.manual_scan)
    }

    override fun onFlashTurnOn() {
        ivFlash?.setImageResource(R.drawable.ic_flash_off)
        camera?.cameraControl?.enableTorch(true)
    }

    override fun onFlashTurnOff() {
        ivFlash?.setImageResource(R.drawable.ic_flash_on)
        camera?.cameraControl?.enableTorch(false)
    }

    private fun initView() {
        context?.let {
            val isHasFlash = it.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            if (isHasFlash) {
                ivFlash?.visibility = View.VISIBLE
            } else {
                ivFlash?.visibility = View.GONE
            }
        }
        tvCancel?.setOnClickListener { activity?.onBackPressed() }
        tvAutoScan?.setOnClickListener { presenter.toggleAutoScan() }
        ivFlash?.setOnClickListener { presenter.toggleFlash() }
    }

    private fun startCamera() {
        context?.let {
            cameraProviderFuture = ProcessCameraProvider.getInstance(it)
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(LensFacing.BACK)
                    .build()
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                preview.previewSurfaceProvider = previewView.previewSurfaceProvider
                camera = cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
//                    preview,
                    getImageAnalysis(it)
                )
            }, ContextCompat.getMainExecutor(it))
        }
    }

    private fun getImageAnalysis(context: Context): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.BackpressureStrategy.KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//            .setTargetResolution(Size(1280, 720))
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context),
                    ImageAnalysis.Analyzer { image, rotationDegrees ->
                        val original = ImageUtil.imageToBitmap(image, rotationDegrees.toFloat())
                        val quadrilateral = imageProcessor.findDocument(original)
                        // END
                        (context as Activity).runOnUiThread {
                            ivPreView?.setImageBitmap(imageProcessor.drawDocumentBox(original, quadrilateral))
//                            rectView?.drawRect(
//                                imageProcessor.convertToPreviewPoint(quadrilateral, ivPreView.width, ivPreView.height))
                        }
                        image.close()
                    }
                )
            }
    }
}

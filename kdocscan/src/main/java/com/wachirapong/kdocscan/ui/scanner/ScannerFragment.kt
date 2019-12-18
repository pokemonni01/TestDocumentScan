package com.wachirapong.kdocscan.ui.scanner


import android.content.pm.PackageManager
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
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.fragment_scanner.previewView

class ScannerFragment : BaseFragment(), ScannerContract.View {

    companion object {
        fun initInstance() = ScannerFragment()
    }

    private val presenter = ScannerPresenter()
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
                val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
                preview.previewSurfaceProvider = previewView.previewSurfaceProvider
                camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
            }, ContextCompat.getMainExecutor(it))
        }
    }

//    private fun getImageCapture(): ImageCapture {
//        val imageCapture = ImageCapture.Builder()
//        return imageCapture
//    }
}

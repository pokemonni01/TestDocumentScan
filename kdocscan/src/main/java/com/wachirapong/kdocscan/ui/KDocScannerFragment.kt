package com.wachirapong.kdocscan.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.LensFacing
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.wachirapong.kdocscan.R
import kotlinx.android.synthetic.main.fragment_kdoc_scanner.*

class KDocScannerFragment : Fragment() {

    companion object {
        fun initInstance() = KDocScannerFragment()
    }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kdoc_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startCamera() {
        // New
        context?.let {
            cameraProviderFuture = ProcessCameraProvider.getInstance(it)
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder().requireLensFacing(LensFacing.BACK).build()
                val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
                preview.previewSurfaceProvider = previewView.previewSurfaceProvider
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
            }, ContextCompat.getMainExecutor(it))
        }
    }
}

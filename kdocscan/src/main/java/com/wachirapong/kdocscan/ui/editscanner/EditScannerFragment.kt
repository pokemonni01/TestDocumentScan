package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.LensFacing
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerFragment
import kotlinx.android.synthetic.main.fragment_kdoc_scanner.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class EditScannerFragment : BaseFragment(), EditScannerContract.View {

    companion object {
        fun initInstance() = EditScannerFragment()
    }

    private val presenter = EditScannerPresenter()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

    private fun startCamera() {
        context?.let {
            cameraProviderFuture = ProcessCameraProvider.getInstance(it)
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(LensFacing.BACK).build()
                val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
                preview.previewSurfaceProvider = previewView.previewSurfaceProvider
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
            }, ContextCompat.getMainExecutor(it))
        }
    }

    override fun onOpenCVConnectionFailed() { }

    override fun onAfterViewCreated() { }

    private fun initView() {

    }

    override fun showImage(pathName: String) {
        imagePathBitmap(pathName)
    }

    private fun imagePathBitmap(pathName: String) : String {
        val file = File(pathName)
        val fileInputStream = FileInputStream(file)
        val bitmap = BitmapFactory.decodeStream(fileInputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArrayImg = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArrayImg, Base64.DEFAULT)
    }
}

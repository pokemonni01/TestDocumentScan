package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerFragment
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
        //KDocScannerFragment().startCamera()
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

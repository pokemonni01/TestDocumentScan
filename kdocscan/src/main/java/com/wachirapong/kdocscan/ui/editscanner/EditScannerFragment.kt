package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.data.ScannedDocument
import com.wachirapong.kdocscan.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_edit_scanner.*
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream


private const val BUNDLE_SCANNED_DOCUMENT = "BUNDLE_SCANNED_DOCUMENT"

class EditScannerFragment : BaseFragment(), EditScannerContract.View {

    companion object {
        fun initInstance(scannedDocument: ScannedDocument) = EditScannerFragment().apply {
            arguments = bundleOf(BUNDLE_SCANNED_DOCUMENT to scannedDocument)
        }
    }

    private val presenter: EditScannerContract.Presenter by inject()
    private var bitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.provideView(this)
        arguments?.let {
            presenter.showScannedDocument(it.getParcelable(BUNDLE_SCANNED_DOCUMENT))
        }
        initView()
    }

    override fun showImage(image: Bitmap) {
        imageView.post {
            imageView.setImageBitmap(image)
            presenter.getDocumentPoint(imageView.width, imageView.height)
        }
    }

    override fun showDocumentPoints(pointFMap: Map<Int, PointF>) {
        polygonView?.points = pointFMap
    }

    private fun initView() {
        btnNext?.setOnClickListener {

        }
    }

    private fun imagePathBitmap(pathName: String): String {
        val file = File(pathName)
        val fileInputStream = FileInputStream(file)
        val bitmap = BitmapFactory.decodeStream(fileInputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArrayImg = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArrayImg, Base64.DEFAULT)
    }
}

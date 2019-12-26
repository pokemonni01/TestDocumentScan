package com.wachirapong.kdocscan.ui.editscanner

import android.content.Context
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
import com.wachirapong.kdocscan.ui.scanner.ScannerFragment
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerActivity
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
    private var listener: EditScannerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EditScannerListener) {
            listener = context
        }
    }

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

    override fun openReviewDoc(file: File) {
        listener?.onCropImageSuccess(file)
    }

    private fun initView() {
        btnNext?.setOnClickListener {
            presenter.cropImage(polygonView.pointsWithOutOrder)
        }
        btnBack?.setOnClickListener {
            (context as KDocScannerActivity).onBackPressed()
        }
    }

    interface EditScannerListener {
        fun onCropImageSuccess(file: File)
    }
}

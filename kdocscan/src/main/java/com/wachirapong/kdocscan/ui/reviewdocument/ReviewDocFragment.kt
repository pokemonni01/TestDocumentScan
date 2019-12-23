package com.wachirapong.kdocscan.ui.reviewdocument

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.BaseFragment
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerActivity
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerFragment
import kotlinx.android.synthetic.main.fragment_review_document.*
import org.koin.android.ext.android.inject
import java.io.File

class ReviewDocFragment : BaseFragment(), ReviewDocContract.View {

    private val presenter: ReviewDocContract.Presenter by inject()
    lateinit var pathFile: String

    companion object {
        private const val ARGUMENT_PATH_FILE = "pathFile"
        fun initInstance(pathFile: String): ReviewDocFragment {
            val fragment = ReviewDocFragment()
            val args = Bundle().apply {
                putString(ARGUMENT_PATH_FILE, pathFile)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pathFile = it.getString(ARGUMENT_PATH_FILE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.provideView(this)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.mode -> {
                    presenter.changeColor()
                }
                R.id.rotate -> {
                    presenter.rotateImage()
                }
            }
            false
        }

        ivBack.setOnClickListener {
            (context as KDocScannerActivity).onBackPressed()
        }

        tvdone.setOnClickListener {
            presenter.saveImage()
            Toast.makeText(context, "done", Toast.LENGTH_LONG).show()
        }

        presenter.readImageFile(pathFile)
    }

    override fun showImage(bm: Bitmap) {
        ivPreView.setImageBitmap(bm)
    }

}
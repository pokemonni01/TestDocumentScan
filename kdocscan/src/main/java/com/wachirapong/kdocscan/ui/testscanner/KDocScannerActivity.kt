package com.wachirapong.kdocscan.ui.testscanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.data.ScannedDocument
import com.wachirapong.kdocscan.ui.editscanner.EditScannerFragment
import com.wachirapong.kdocscan.ui.reviewdocument.ReviewDocFragment
import com.wachirapong.kdocscan.ui.scanner.ScannerFragment
import java.io.File


class KDocScannerActivity : AppCompatActivity(), ScannerFragment.ScannerListener,
    EditScannerFragment.EditScannerListener {

    companion object {

        fun getStartIntent(context: Context): Intent {
            return Intent(context, KDocScannerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kdoc_scanner)
        requestPermission()
    }

    override fun onDocumentDetected(scannedDocument: ScannedDocument) {
        startEditScan(scannedDocument)
    }

    override fun onCropImageSuccess(file: File) {
        startReviewDoc(file.absolutePath)
    }

    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    startScanner()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }).check()
    }

    private fun startScanner() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                ScannerFragment.initInstance()
            )
            .addToBackStack(null)
            .commit()
    }

    private fun startEditScan(scannedDocument: ScannedDocument) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                EditScannerFragment.initInstance(scannedDocument)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun startReviewDoc(documentImagePath: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                ReviewDocFragment.initInstance(documentImagePath)
            )
            .addToBackStack(null)
            .commit()
    }
}

package com.wachirapong.kdocscan.ui.testscanner

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.scanner.ScannerFragment

class KDocScannerActivity : AppCompatActivity() {

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, KDocScannerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kdoc_scanner)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container,
                ScannerFragment.initInstance()
            )
            .addToBackStack(null)
            .commit()
    }
}

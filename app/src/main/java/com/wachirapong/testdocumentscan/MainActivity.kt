package com.wachirapong.testdocumentscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wachirapong.kdocscan.KDocScanner

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        CVScanner.startScanner(this, false, 9002)
        KDocScanner.startKDocScanner(this)
    }
}

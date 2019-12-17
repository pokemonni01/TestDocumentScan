package com.wachirapong.kdocscan

import android.app.Activity
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerActivity

object KDocScanner {

    fun startKDocScanner(activity: Activity) {
        activity.startActivity(KDocScannerActivity.getStartIntent(activity))
    }
}
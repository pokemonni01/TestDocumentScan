package com.wachirapong.kdocscan.ui.testscanner

import android.graphics.Bitmap

interface KDocScannerContract {
    interface View {

    }

    interface Presenter {
        fun detectDocument(bitmap: Bitmap)
    }
}
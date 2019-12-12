package com.wachirapong.kdocscan.ui.scanner

import android.graphics.Bitmap

interface KDocScannerContract {
    interface View {

    }

    interface Presenter {
        fun detectDocument(bitmap: Bitmap)
    }
}
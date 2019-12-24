package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.PointF
import com.wachirapong.kdocscan.data.ScannedDocument

interface EditScannerContract {
    interface View {
        fun showImage(image: Bitmap)
        fun showDocumentPoints(pointFMap: Map<Int, PointF> )
    }

    interface Presenter {
        fun provideView(view: View)
        fun showScannedDocument(scannedDocument: ScannedDocument?)
        fun getDocumentPoint(viewWidth: Int, viewHeight: Int)
    }
}
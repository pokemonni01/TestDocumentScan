package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.PointF
import com.wachirapong.kdocscan.data.ScannedDocument
import java.io.File

interface EditScannerContract {
    interface View {
        fun showImage(image: Bitmap)
        fun showDocumentPoints(pointFMap: Map<Int, PointF>)
        fun openReviewDoc(file: File)
    }

    interface Presenter {
        fun provideView(view: View)
        fun showScannedDocument(scannedDocument: ScannedDocument?)
        fun getDocumentPoint(viewWidth: Int, viewHeight: Int)
        fun cropImage(pointList: List<PointF>)
    }
}
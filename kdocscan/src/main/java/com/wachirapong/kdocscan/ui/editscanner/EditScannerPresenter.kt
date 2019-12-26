package com.wachirapong.kdocscan.ui.editscanner

import android.graphics.Bitmap
import android.graphics.PointF
import com.wachirapong.kdocscan.data.DocumentPoint
import com.wachirapong.kdocscan.data.ScannedDocument
import com.wachirapong.kdocscan.manager.FileManager
import com.wachirapong.kdocscan.util.ImageProcessor
import org.opencv.core.Point

class EditScannerPresenter(
    private val fileManager: FileManager,
    private val imageProcessor: ImageProcessor
) : EditScannerContract.Presenter {

    private var view: EditScannerContract.View? = null
    private var scannedDocument: ScannedDocument? = null
    private var ratio = 1f

    var image: Bitmap? = null

    override fun provideView(view: EditScannerContract.View) {
        this.view = view
    }

    override fun showScannedDocument(scannedDocument: ScannedDocument?) {
        this.scannedDocument = scannedDocument
        fileManager.loadBitmapFromStorage(
            absolutePath = scannedDocument?.imageAbsolutePath ?: "",
            onSuccess = {
                image = it
                view?.showImage(it)
            },
            onFail = {
                // TODO On Fail
            }
        )
    }

    override fun getDocumentPoint(viewWidth: Int, viewHeight: Int) {
        image?.let { image ->
            imageProcessor.findDocument(image)?.let {
                val quadrilateral = imageProcessor.convertQuadrilateralToOriginalSize(it)
                ratio = image.width.toFloat() / viewWidth
                view?.showDocumentPoints(
                    hashMapOf(
                        0 to quadrilateral.points?.get(0).toPointF(ratio),
                        1 to quadrilateral.points?.get(1).toPointF(ratio),
                        2 to quadrilateral.points?.get(2).toPointF(ratio),
                        3 to quadrilateral.points?.get(3).toPointF(ratio)
                    )
                )
            }
        }
    }

    override fun cropImage(pointList: List<PointF>) {
        image?.let {
            fileManager.saveBitmapToStorage(
                bitmap = imageProcessor.fourPointTransform(it, pointList, ratio.toDouble()),
                fileName = "pic2.jpg",
                quality = 100,
                onSuccess = { file -> view?.openReviewDoc(file) },
                onFail = { }
            )
        }
    }

    private fun DocumentPoint?.toPointF(ratio: Float = 1f): PointF {
        return PointF((this?.x?.toFloat() ?: 0f) * ratio, (this?.y?.toFloat() ?: 0f) * ratio)
    }

    private fun Point?.toPointF(ratio: Float = 1f): PointF {
        return PointF((this?.x?.toFloat() ?: 0f) / ratio, (this?.y?.toFloat() ?: 0f) / ratio)
    }
}
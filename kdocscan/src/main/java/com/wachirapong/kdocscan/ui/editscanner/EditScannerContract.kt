package com.wachirapong.kdocscan.ui.editscanner

interface EditScannerContract {
    interface View {
        fun showImage(pathName: String)
    }

    interface Presenter {
        fun provideView(view: View)
        fun toggleBackButton()
        fun previewQuadrilateralSelection()
        fun toggleNextButton()
        fun saveImage()
    }
}
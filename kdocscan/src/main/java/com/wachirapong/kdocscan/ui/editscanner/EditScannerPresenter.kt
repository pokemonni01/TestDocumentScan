package com.wachirapong.kdocscan.ui.editscanner

class EditScannerPresenter: EditScannerContract.Presenter {

    private var view: EditScannerContract.View? = null

    override fun provideView(view: EditScannerContract.View) {
        this.view = view
    }

    override fun toggleBackButton() {
        //start camera preview
        //KDocScannerFragment().startCamera()
    }

    override fun previewQuadrilateralSelection() {
        //call QuadrilateralSelectionImageView
    }

    override fun toggleNextButton() {
        //save image to pic2
    }

    override fun saveImage() {
        //save image
    }

}
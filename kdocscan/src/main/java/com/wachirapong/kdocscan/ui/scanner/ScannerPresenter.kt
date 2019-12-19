package com.wachirapong.kdocscan.ui.scanner

class ScannerPresenter: ScannerContract.Presenter {

    private var view: ScannerContract.View? = null
    private var isAutoScan = true
    private var isFlashTurnOn = false

    override fun provideView(view: ScannerContract.View) {
        this.view = view
    }

    override fun toggleAutoScan() {
        isAutoScan = !isAutoScan
        if (isAutoScan) {
            view?.showAutoScan()
        } else {
            view?.showManualScan()
        }
    }

    override fun toggleFlash() {
        isFlashTurnOn = !isFlashTurnOn
        if (isFlashTurnOn) {
            view?.onFlashTurnOn()
        } else {
            view?.onFlashTurnOff()
        }
    }

    override fun captureImage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveBitmapToStorage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
package com.wachirapong.kdocscan.ui.scanner

interface ScannerContract {
    interface View {
        fun showAutoScan()
        fun showManualScan()
        fun onFlashTurnOn()
        fun onFlashTurnOff()
        fun captureImage()
        fun goToEditScan()
    }

    interface Presenter {
        fun provideView(view: View)
        fun toggleAutoScan()
        fun toggleFlash()
        fun captureImage()
        fun saveBitmapToStorage()
    }
}
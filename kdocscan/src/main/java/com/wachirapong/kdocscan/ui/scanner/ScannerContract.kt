package com.wachirapong.kdocscan.ui.scanner

interface ScannerContract {
    interface View {
        fun showAutoScan()
        fun showManualScan()
        fun onFlashTurnOn()
        fun onFlashTurnOff()
    }

    interface Presenter {
        fun provideView(view: View)
        fun toggleAutoScan()
        fun toggleFlash()
    }
}
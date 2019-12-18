package com.wachirapong.kdocscan.di

import com.wachirapong.kdocscan.ui.scanner.ScannerContract
import com.wachirapong.kdocscan.ui.scanner.ScannerPresenter
import org.koin.dsl.module

private val documentScanModule = module {
    factory<ScannerContract.Presenter> { ScannerPresenter() }
}

val modules = listOf(documentScanModule)
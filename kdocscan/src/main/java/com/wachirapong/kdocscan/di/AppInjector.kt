package com.wachirapong.kdocscan.di

import com.wachirapong.kdocscan.ui.reviewdocument.ReviewDocContract
import com.wachirapong.kdocscan.ui.reviewdocument.ReviewDocPresenter
import com.wachirapong.kdocscan.ui.scanner.ScannerContract
import com.wachirapong.kdocscan.ui.scanner.ScannerPresenter
import com.wachirapong.kdocscan.util.ImageProcessor
import org.koin.dsl.module

private val documentScanModule = module {
    factory { ImageProcessor() }
    factory<ScannerContract.Presenter> { ScannerPresenter() }
    factory<ReviewDocContract.Presenter> { ReviewDocPresenter() }
}

val modules = listOf(documentScanModule)
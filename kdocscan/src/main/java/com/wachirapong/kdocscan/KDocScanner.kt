package com.wachirapong.kdocscan

import android.app.Activity
import com.wachirapong.kdocscan.di.modules
import com.wachirapong.kdocscan.ui.testscanner.KDocScannerActivity
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

object KDocScanner {

    fun startKDocScanner(activity: Activity) {
        // TODO need to move how to init koin
        startKoin {
            // Android context
            androidContext(activity.applicationContext)
            // modules
            modules(modules)
        }
        activity.startActivity(KDocScannerActivity.getStartIntent(activity))
    }
}
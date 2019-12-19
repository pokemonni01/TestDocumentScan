package com.wachirapong.kdocscan.manager

import android.graphics.Bitmap
import android.net.Uri

interface FileManager {
    fun saveBitmapToStorage(bitmap: Bitmap, fileName: String, onSuccess: (uri: Uri) -> Unit, onFail: (throwable: Throwable) -> Unit)
    fun loadBitmapFromStorage(fileName: String, onSuccess: (image: Bitmap) -> Unit, onFail: (throwable: Throwable) -> Unit)
}

class FileManagerImpl: FileManager {

    override fun saveBitmapToStorage(
        bitmap: Bitmap,
        fileName: String,
        onSuccess: (uri: Uri) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    ) {

    }

    override fun loadBitmapFromStorage(
        fileName: String,
        onSuccess: (image: Bitmap) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    ) {

    }
}
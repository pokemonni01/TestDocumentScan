package com.wachirapong.kdocscan.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

interface FileManager {
    fun saveBitmapToStorage(
        bitmap: Bitmap,
        fileName: String,
        onSuccess: (file: File) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    )

    fun loadBitmapFromStorage(
        absolutePath: String,
        onSuccess: (image: Bitmap) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    )
}

class FileManagerImpl(private val context: Context) : FileManager {

    override fun saveBitmapToStorage(
        bitmap: Bitmap,
        fileName: String,
        onSuccess: (file: File) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    ) {
        try {
            //create a file to write bitmap data
            val file = File(ContextCompat.getExternalFilesDirs(context, null).first(), fileName)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            FileOutputStream(file).also {
                it.write(bitmapData)
                it.flush()
                it.close()
            }
            onSuccess(file)
        } catch (e: Exception) {
            onFail(e)
        }
    }

    override fun loadBitmapFromStorage(
        absolutePath: String,
        onSuccess: (image: Bitmap) -> Unit,
        onFail: (throwable: Throwable) -> Unit
    ) {
        try {
            var bitmap = BitmapFactory.decodeFile(absolutePath)
            val exif = ExifInterface(absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            onSuccess(bitmap)
        } catch (e: Exception) {
            onFail(e)
        }
    }
}
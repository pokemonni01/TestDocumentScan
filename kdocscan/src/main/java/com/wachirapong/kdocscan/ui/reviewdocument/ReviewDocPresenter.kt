package com.wachirapong.kdocscan.ui.reviewdocument

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType.CV_8UC4
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.FileOutputStream
import java.lang.Exception
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.wachirapong.kdocscan.manager.FileManager
import java.io.File


class ReviewDocPresenter(private val fileManager: FileManager) : ReviewDocContract.Presenter {

    lateinit var view: ReviewDocContract.View
    lateinit var oriImg: Bitmap
    lateinit var grayImg: Bitmap
    lateinit var colorImg: Bitmap
    lateinit var oriMat: Mat
    lateinit var grayMat: Mat
    var colorMode: Boolean = false
    var flipMode: Boolean = false
    lateinit var saveFilePath: String

    override fun provideView(view: ReviewDocContract.View) {
        this.view = view
    }

    override fun changeColor() {
        colorMode = !colorMode
        if (colorMode) {
            view.showImage(grayImg)
        } else {
            view.showImage(colorImg)
        }
    }

    override fun rotateImage() {
        flipMode = !flipMode
        if (flipMode) {
            grayImg = Bitmap.createBitmap(oriImg.height, oriImg.width, Bitmap.Config.ARGB_8888)
            colorImg = Bitmap.createBitmap(oriImg.height, oriImg.width, Bitmap.Config.ARGB_8888)
        } else {
            grayImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
            colorImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
        }

        rotateBitmapImage(grayMat, grayImg)
        rotateBitmapImage(oriMat, colorImg)

        if (colorMode) {
            view.showImage(grayImg)
        } else {
            view.showImage(colorImg)
        }
    }

    override fun readImageFile(path: String) {
        saveFilePath = File(path).parent
        oriImg = BitmapFactory.decodeFile(path)
        try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            oriImg = Bitmap.createBitmap(oriImg, 0, 0, oriImg.width, oriImg.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        grayImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
        colorImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
        oriMat = Mat(colorImg.width, colorImg.height, CV_8UC4)
        grayMat = Mat(grayImg.width, grayImg.width, CV_8UC4)

        Utils.bitmapToMat(oriImg, grayMat)
        Imgproc.cvtColor(grayMat, grayMat, COLOR_RGB2GRAY, 4)
        Utils.matToBitmap(grayMat, grayImg)

        Utils.bitmapToMat(oriImg, oriMat)
        Utils.matToBitmap(oriMat, colorImg)

        view.showImage(oriImg)
    }

    override fun saveImage() {
        if (colorMode) {
            saveImageToFile(grayImg)
        } else {
            saveImageToFile(colorImg)
        }
        removeImage()
    }

    private fun saveImageToFile(img: Bitmap) {
        img.let {
            fileManager.saveBitmapToStorage(
                bitmap = img,
                fileName = "pic2.jpg",
                quality = 80,
                onSuccess = {},
                onFail = {}
            )
        }
    }

    private fun removeImage() {
        fileManager.removeImageFromStorage(
            fileName = "Pic1.jpg"
        )
    }

    private fun rotateBitmapImage(src: Mat, bm: Bitmap) {
        Core.transpose(src, src)
        Core.flip(src, src, 1)
        Utils.matToBitmap(src, bm)
    }
}
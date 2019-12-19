package com.wachirapong.kdocscan.ui.reviewdocument

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType.CV_8UC4
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.FileOutputStream


class ReviewDocPresenter : ReviewDocContract.Presenter {

    lateinit var view: ReviewDocContract.View
    lateinit var oriImg: Bitmap
    lateinit var grayImg: Bitmap
    lateinit var colorImg: Bitmap
    lateinit var oriMat: Mat
    lateinit var grayMat: Mat
    var colorMode: Boolean = false
    var flipMode: Boolean = false


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
        oriImg = BitmapFactory.decodeFile(path)
        grayImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
        colorImg = Bitmap.createBitmap(oriImg.width, oriImg.height, Bitmap.Config.ARGB_8888)
        oriMat = Mat(colorImg.width, colorImg.height, CV_8UC4)
        grayMat = Mat(grayImg.width, grayImg.width, CV_8UC4)

        Utils.bitmapToMat(oriImg, grayMat)
        Imgproc.cvtColor(grayMat, grayMat, COLOR_RGB2GRAY, 4)
        Utils.matToBitmap(grayMat, grayImg)

        Utils.bitmapToMat(oriImg, oriMat)
        Utils.matToBitmap(oriMat, colorImg)

        view.showImage(colorImg)
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
        val file =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + "testDoc2.jpg")
        val outStream = FileOutputStream(file)
        img.compress(Bitmap.CompressFormat.JPEG, 80, outStream)
        outStream.flush()
        outStream.close()
    }

    private fun removeImage() {
        val file =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + "buff.jpg")
        if (file.exists()) {
            file.delete()
        }
    }

    private fun rotateBitmapImage(src: Mat, bm: Bitmap) {
        Core.transpose(src, src)
        Core.flip(src, src, 1)
        Utils.matToBitmap(src, bm)
    }
}
package com.wachirapong.kdocscan.ui.reviewdocument

import android.graphics.Bitmap

interface ReviewDocContract {
    interface View {
        fun showImage(bm: Bitmap)
    }

    interface Presenter {
        fun provideView(view: View)
        fun changeColor()
        fun rotateImage()
        fun readImageFile(path: String)
        fun saveImage()
    }
}
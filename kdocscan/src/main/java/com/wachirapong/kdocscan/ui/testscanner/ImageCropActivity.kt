package com.wachirapong.kdocscan.ui.testscanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.editscanner.NativeClass
import com.wachirapong.kdocscan.ui.editscanner.PolygonView
import org.opencv.core.MatOfPoint2f
import java.util.*

class ImageCropActivity : AppCompatActivity() {

    private val selectedImageBitmap: Bitmap? = null
    private var tempBitmapOrginal: Bitmap? = null
    private val imageView: ImageView? = null
    private val holderImageCrop: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kdoc_scanner)
        initializeElement()
    }

    private fun initializeElement() {
        holderImageCrop?.post {
            initializeCropping()
        }
    }

    private fun initializeCropping() {

        /*selectedImageBitmap = ScannerConstants.selectedImageBitmap
        tempBitmapOrginal = selectedImageBitmap.copy(selectedImageBitmap.getConfig(), true)
        ScannerConstants.selectedImageBitmap = null*/

        //get width and height of Bitmap
        val scaledBitmap: Bitmap? =
            selectedImageBitmap?.let {
                scaledBitmap(it, holderImageCrop!!.width, holderImageCrop.height)
            }

        //set scale of bitmap
        imageView?.setImageBitmap(scaledBitmap)

        //image drawable
        val tempBitmap = (imageView?.drawable as BitmapDrawable).bitmap
        var pointFs: Map<Int?, PointF?>? = null

        try {

            //check edge
            pointFs = getEdgePoints(tempBitmap)
            polygonView?.setPoints(pointFs)
            polygonView?.visibility = View.VISIBLE

            //padding
            val padding = resources.getDimension(R.dimen.scanPadding).toInt()

            //show rectangle
            val layoutParams = FrameLayout.LayoutParams(
                tempBitmap.width + 2 * padding,         //ทำ padding
                tempBitmap.height + 2 * padding
            )

            //set gravity
            layoutParams.gravity = Gravity.CENTER
            polygonView?.layoutParams = layoutParams
            polygonView?.setPointColor(android.R.color.holo_blue_dark)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val m = Matrix()
        m.setRectToRect(
            RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()),
            RectF(0F, 0F, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    @Throws(java.lang.Exception::class)
    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int?, PointF?>? {
        val pointFs: List<PointF>? = getContourEdgePoints(tempBitmap)
        return pointFs?.let {
            orderedValidEdgePoints(tempBitmap, it)
        }
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF>? {
        var point2f: MatOfPoint2f = NativeClass().getPoint(tempBitmap)

        if (point2f == null)
            point2f = MatOfPoint2f()

        val points =
            listOf(*point2f.toArray())
        val result: MutableList<PointF> = ArrayList()

        for (i in points.indices) {
            result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
        }

        return result
    }

    private val polygonView: PolygonView? = null

    private fun orderedValidEdgePoints(tempBitmap: Bitmap, pointFs: List<PointF>): Map<Int?, PointF?>? {
        var orderedPoints: Map<Int?, PointF?>? = polygonView?.getOrderedPoints(pointFs)
        /*if (!polygonView?.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }*/
        return orderedPoints
    }

}
package com.wachirapong.kdocscan.ui.testscanner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.wachirapong.kdocscan.R
import com.wachirapong.kdocscan.ui.editscanner.QuadrilateralSelectionImageView
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class KDocScannerActivity : AppCompatActivity() {

    //call view 4 points
    private var mSelectionImageView: QuadrilateralSelectionImageView? = null

    //get bitmap from pathName
    var mBitmap: Bitmap? = null

    //Result from transform bitmap
    private var mResult: Bitmap? = null

    //Dialog result
    //var mResultDialog: MaterialDialog? = null

    //Calling
    var mButton: Button? = null
    var btnNext: Button? = null
    var btnBack: Button? = null

    companion object {

        //fix value
        const val MAX_HEIGHT: Int = 500
        const val PICK_IMAGE_REQUEST = 1

        fun getStartIntent(context: Context): Intent {
            return Intent(context, KDocScannerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_kdoc_scanner)
        setContentView(R.layout.fragment_edit_kdoc_scanner)
        editScanDocument()

        /*supportFragmentManager
            .beginTransaction()
            .replace(R.id.container,
                ScannerFragment.initInstance()
            )
            .addToBackStack(null)
            .commit()*/
    }

    fun editScanDocument(){
        mSelectionImageView = (mSelectionImageView)?.findViewById(R.id.polygonView)
        mButton = findViewById(R.id.button)

        btnNext?.setOnClickListener {
            //open new activity
        }

        btnBack?.setOnClickListener {
            //open camera
        }

        mButton?.setOnClickListener {
            val points = mSelectionImageView?.getPoints()

            //Image must not be null
            if (mBitmap != null) {

                //transform
                val orig = Mat()
                Utils.bitmapToMat(mBitmap, orig)
                val transformed: Mat? = perspectiveTransform(orig, points as List<PointF>)

                //give the transformed
                mResult = transformed?.let {
                        it1 -> applyThreshold(it1)
                }

                //show the image
                /*if (mResultDialog.getCustomView() != null) {
                    val photoView: PhotoView =
                        mResultDialog.getCustomView().findViewById(R.id.imageView) as PhotoView
                    photoView.setImageBitmap(mResult)
                    mResultDialog.show()
                }*/

                //Release
                orig.release()
                transformed?.release()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                mSelectionImageView?.setImageBitmap(
                    getResizedBitmap(mBitmap!!, MAX_HEIGHT)
                )
                val points: List<PointF?> = findPoints()
                mSelectionImageView?.setPoints(points)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //-----------------------------------------------find rectangle----------------------------------------------------
    open fun findPoints(): List<PointF?> {
        var result: MutableList<PointF?>? = null
        val image = Mat()
        val orig = Mat()

        Utils.bitmapToMat(getResizedBitmap(mBitmap!!, MAX_HEIGHT), image)
        Utils.bitmapToMat(mBitmap, orig)

        val edges: Mat ?= edgeDetection(image)
        val largest: MatOfPoint2f ?= edges?.let {
            findLargestContour(it)
        }

        if (largest != null) {
            val points = sortPoints(largest.toArray())
            result = ArrayList()

            //four points
            result.add(
                PointF(
                    java.lang.Double.valueOf(points?.get(0)!!.x).toFloat(),
                    java.lang.Double.valueOf(points[0]!!.y).toFloat()
                )
            )
            result.add(
                PointF(
                    java.lang.Double.valueOf(points[1]!!.x).toFloat(),
                    java.lang.Double.valueOf(points[1]!!.y).toFloat()
                )
            )
            result.add(
                PointF(
                    java.lang.Double.valueOf(points[2]!!.x).toFloat(),
                    java.lang.Double.valueOf(points[2]!!.y).toFloat()
                )
            )
            result.add(
                PointF(
                    java.lang.Double.valueOf(points[3]!!.x).toFloat(),
                    java.lang.Double.valueOf(points[3]!!.y).toFloat()
                )
            )

            largest.release()

        } else {
            //Timber.d("Can't find rectangle!")
        }

        edges?.release()
        image.release()
        orig.release()

        return result!!
    }

    //----------------------------------------------find largest contour-----------------------------------------------
    open fun findLargestContour(src: Mat): MatOfPoint2f? {
        val contours: List<MatOfPoint> = ArrayList()
        Imgproc.findContours(
            src,
            contours,
            Mat(),
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // Get the 5 largest contours
        Collections.sort(contours) { o1, o2 ->
            val area1 = Imgproc.contourArea(o1)
            val area2 = Imgproc.contourArea(o2)
            (area2 - area1).toInt()
        }

        if (contours.size > 5) contours.subList(4, contours.size - 1)
        var largest: MatOfPoint2f? = null
        for (contour in contours) {
            val approx = MatOfPoint2f()
            val c = MatOfPoint2f()
            contour.convertTo(c, CvType.CV_32FC2)
            Imgproc.approxPolyDP(c, approx, Imgproc.arcLength(c, true) * 0.02, true)

            // the contour has 4 points, it's valid
            if (approx.total() == 4L && Imgproc.contourArea(contour) > 150) {
                largest = approx
                break
            }

        }
        return largest
    }

    //--------------------------------------------------edgeDetection--------------------------------------------------
    open fun edgeDetection(src: Mat): Mat? {
        val edges = Mat()
        Imgproc.cvtColor(src, edges, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(edges, edges, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(edges, edges, 75.0, 200.0)
        return edges
    }

    //-----------------------------------------------get bitmap size---------------------------------------------------
    open fun getResizedBitmap(bitmap: Bitmap, maxHeight: Int): Bitmap? {
        val ratio = bitmap.height / maxHeight.toDouble()
        val width = (bitmap.width / ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, maxHeight, false)
    }

    //------------------------------------------------Transform--------------------------------------------------------
    private fun perspectiveTransform(src: Mat, points: List<PointF>): Mat? {
        val point1 = Point(points[0].x.toDouble(), points[0].y.toDouble())
        val point2 = Point(points[1].x.toDouble(), points[1].y.toDouble())
        val point3 = Point(points[2].x.toDouble(), points[2].y.toDouble())
        val point4 = Point(points[3].x.toDouble(), points[3].y.toDouble())
        val pts =
            arrayOf(point1, point2, point3, point4)
        return fourPointTransform(src, sortPoints(pts))
    }

    private fun fourPointTransform(src: Mat, pts: Array<Point?>?): Mat? {
        val ratio = src.size().height / Companion.MAX_HEIGHT as Double

        val ul = pts?.get(0)
        val ur = pts?.get(1)
        val lr = pts?.get(2)
        val ll = pts?.get(3)

        val widthA = sqrt(
            (lr!!.x - ll!!.x).pow(2.0) + (lr.y - ll.y).pow(2.0)
        )
        val widthB = sqrt(
            (ur!!.x - ul!!.x).pow(2.0) + (ur.y - ul.y).pow(2.0)
        )
        val maxWidth = max(widthA, widthB) * ratio
        val heightA = sqrt(
            (ur.x - lr.x).pow(2.0) + (ur.y - lr.y).pow(2.0)
        )
        val heightB = sqrt(
            (ul.x - ll.x).pow(2.0) + (ul.y - ll.y).pow(2.0)
        )
        val maxHeight = max(heightA, heightB) * ratio
        val resultMat = Mat(
            java.lang.Double.valueOf(maxHeight).toInt(),
            java.lang.Double.valueOf(maxWidth).toInt(),
            CvType.CV_8UC4
        )
        val srcMat = Mat(4, 1, CvType.CV_32FC2)
        val dstMat = Mat(4, 1, CvType.CV_32FC2)
        srcMat.put(0, 0,
            ul.x * ratio,
            ul.y * ratio,
            ur.x * ratio,
            ur.y * ratio,
            lr.x * ratio,
            lr.y * ratio,
            ll.x * ratio,
            ll.y * ratio
        )
        dstMat.put(0, 0, 0.0, 0.0, maxWidth, 0.0, maxWidth, maxHeight, 0.0, maxHeight)
        val M =
            Imgproc.getPerspectiveTransform(srcMat, dstMat)
        Imgproc.warpPerspective(src, resultMat, M, resultMat.size())
        srcMat.release()
        dstMat.release()
        M.release()
        return resultMat
    }

    private fun sortPoints(src: Array<Point>): Array<Point?>? {
        val srcPoints = ArrayList(listOf(*src))
        val result = arrayOf<Point?>(null, null, null, null)

        val sumComparator =
            Comparator<Point> { lhs, rhs ->
                java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
            }

        val differenceComparator = Comparator<Point> { lhs, rhs ->
                java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
            }

        // Upper left has the minimal sum
        result[0] = Collections.min(srcPoints, sumComparator)
        // Lower right has the maximal sum
        result[2] = Collections.max(srcPoints, sumComparator)
        // Upper right has the minimal difference
        result[1] = Collections.min(srcPoints, differenceComparator)
        // Lower left has the maximal difference
        result[3] = Collections.max(srcPoints, differenceComparator)

        return result
    }

    //--------------------------------------------Threadhold---------------------------------------------
    open fun applyThreshold(src: Mat): Bitmap? {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(src, src, Size(5.0, 5.0), 0.0)
        Imgproc.adaptiveThreshold(
            src,
            src,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )
        val bm = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(src, bm)
        return bm
    }
}

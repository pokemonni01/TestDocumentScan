package com.wachirapong.kdocscan.ui.editscanner

import android.R
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat


class QuadrilateralSelectionImageView @JvmOverloads constructor(context: Context, attrs:AttributeSet?=null, defStyleAttr: Int=0)
    : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mBackgroundPaint: Paint? = null
    private var mBorderPaint: Paint? = null
    private var mCirclePaint: Paint? = null
    private var mSelectionPath: Path? = null
    private var mBackgroundPath: Path? = null

    private var mUpperLeftPoint: PointF? = null
    private var mUpperRightPoint: PointF? = null
    private var mLowerLeftPoint: PointF? = null
    private var mLowerRightPoint: PointF? = null
    private var mLastTouchedPoint: PointF? = null

    init {
        initialView(attrs!!, defStyleAttr)
    }

    /*fun QuadrilateralSelectionImageView(context: Context?) {
        //super(context)
        init(null, 0)
    }

    fun QuadrilateralSelectionImageView(context: Context?, attrs: AttributeSet?) {
        //super(context, attrs)
        init(attrs!!, 0)
    }

    fun QuadrilateralSelectionImageView(context: Context?, attrs: AttributeSet?, defStyle: Int) {
        //super(context, attrs, defStyle)
        init(attrs!!, defStyle)
    }*/

    private fun initialView(attrs: AttributeSet?, defStyle: Int) {

        mBackgroundPaint = Paint()
        mBackgroundPaint?.color = -0x80000000

        mBorderPaint = Paint()
        mBorderPaint?.color = ContextCompat.getColor(context, R.color.holo_blue_dark)
        mBorderPaint?.isAntiAlias = true
        mBorderPaint?.style = Paint.Style.STROKE
        mBorderPaint?.strokeWidth = 8f

        mCirclePaint = Paint()
        mCirclePaint?.color = ContextCompat.getColor(context, R.color.holo_blue_dark)
        mCirclePaint?.isAntiAlias = true
        mCirclePaint?.style = Paint.Style.STROKE
        mCirclePaint?.strokeWidth = 8f

        mSelectionPath = Path()
        mBackgroundPath = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (mUpperLeftPoint == null || mUpperRightPoint == null || mLowerRightPoint == null || mLowerLeftPoint == null) {
            setDefaultSelection()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mSelectionPath?.reset()
        mSelectionPath?.fillType = Path.FillType.EVEN_ODD

        mSelectionPath?.moveTo(mUpperRightPoint!!.x, mUpperLeftPoint!!.y)
        mSelectionPath?.lineTo(mUpperRightPoint!!.x, mUpperRightPoint!!.y)
        mSelectionPath?.lineTo(mLowerRightPoint!!.x, mLowerRightPoint!!.y)
        mSelectionPath?.lineTo(mLowerLeftPoint!!.x, mLowerLeftPoint!!.y)
        mSelectionPath?.close()

        mBackgroundPath?.reset()
        mBackgroundPath?.fillType = Path.FillType.EVEN_ODD
        mBackgroundPath?.addRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Path.Direction.CW
        )
        mBackgroundPath?.addPath(mSelectionPath)


        mBackgroundPath?.let { mBackgroundPaint?.let {
                it1 -> canvas.drawPath(it, it1)
            }
        }

        mSelectionPath?.let { mBorderPaint?.let {
                it1 -> canvas.drawPath(it, it1)
            }
        }

        if (mUpperLeftPoint != null) {
            mCirclePaint?.let { canvas.drawCircle(mUpperLeftPoint!!.x, mUpperLeftPoint!!.y, 30f, it) }
        }

        if (mUpperRightPoint != null) {
            mCirclePaint?.let { canvas.drawCircle(mUpperRightPoint!!.x, mUpperRightPoint!!.y, 30f, it) }
        }

        if (mLowerRightPoint != null) {
            mCirclePaint?.let { canvas.drawCircle(mLowerRightPoint!!.x, mLowerRightPoint!!.y, 30f, it) }
        }

        if (mLowerLeftPoint != null) {
            mCirclePaint?.let { canvas.drawCircle(mLowerLeftPoint!!.x, mLowerLeftPoint!!.y, 30f, it) }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                var isConvex = false
                val eventPoint = PointF(event.x, event.y)

                // Determine if the shape will still be convex when we apply the users next drag
                if (mLastTouchedPoint === mUpperLeftPoint) {
                    isConvex = isConvexQuadrilateral(eventPoint, mUpperRightPoint!!, mLowerRightPoint!!, mLowerLeftPoint!!)
                } else if (mLastTouchedPoint === mUpperRightPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, eventPoint, mLowerRightPoint!!, mLowerLeftPoint!!)
                } else if (mLastTouchedPoint === mLowerRightPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, mUpperRightPoint!!, eventPoint, mLowerLeftPoint!!
                    )
                } else if (mLastTouchedPoint === mLowerLeftPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, mUpperRightPoint!!, mLowerRightPoint!!, eventPoint)
                }
                if (isConvex && mLastTouchedPoint != null) {
                    mLastTouchedPoint?.set(event.x, event.y)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val p = 100
                mLastTouchedPoint =
                    if (event.x < mUpperLeftPoint!!.x + p && event.x > mUpperLeftPoint!!.x - p && event.y < mUpperLeftPoint!!.y + p && event.y > mUpperLeftPoint!!.y - p) {
                        mUpperLeftPoint
                    } else if (event.x < mUpperRightPoint!!.x + p && event.x > mUpperRightPoint!!.x - p && event.y < mUpperRightPoint!!.y + p && event.y > mUpperRightPoint!!.y - p) {
                        mUpperRightPoint
                    } else if (event.x < mLowerRightPoint!!.x + p && event.x > mLowerRightPoint!!.x - p && event.y < mLowerRightPoint!!.y + p && event.y > mLowerRightPoint!!.y - p) {
                        mLowerRightPoint
                    } else if (event.x < mLowerLeftPoint!!.x + p && event.x > mLowerLeftPoint!!.x - p && event.y < mLowerLeftPoint!!.y + p && event.y > mLowerLeftPoint!!.y - p) {
                        mLowerLeftPoint
                    } else {
                        null
                    }
            }
        }
        invalidate()
        return true
    }

    //-------------------------------------------------------Start Map Point to Matrix-------------------------------------------
    //Translate the given point from view coordinates to image coordinates
    private fun viewPointToImagePoint(point: PointF): PointF? {
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        return mapPointToMatrix(point, matrix)
    }

    //Translate the given point from image coordinates to view coordinates
    private fun imagePointToViewPoint(imgPoint: PointF): PointF? {
        return mapPointToMatrix(imgPoint, imageMatrix)
    }

    private fun mapPointToMatrix(point: PointF, matrix: Matrix): PointF? {
        val points = floatArrayOf(point.x, point.y)
        matrix.mapPoints(points)
        return if (points.size > 1) {
            PointF(points[0], points[1])
        } else {
            null
        }
    }

    fun getPoints(): List<PointF?>? {
        val list: MutableList<PointF?> = ArrayList()

        list.add(mUpperLeftPoint?.let {
            viewPointToImagePoint(it)
        })
        list.add(mUpperRightPoint?.let {
            viewPointToImagePoint(it)
        })
        list.add(mLowerRightPoint?.let {
            viewPointToImagePoint(it)
        })
        list.add(mLowerLeftPoint?.let {
            viewPointToImagePoint(it)
        })

        return list
    }
    //-------------------------------------------------------End Map Point to Matrix-------------------------------------------
    /**
     * Set the points in order to control where the selection will be drawn.  The points should
     * be represented in regards to the image, not the view.  This method will translate from image
     * coordinates to view coordinates.
     *
     * NOTE: Calling this method will invalidate the view
     *
     * @param points A list of points. Passing null will set the selector to the default selection.
     */
    fun setPoints(points: List<PointF?>?) {
        if (points != null) {
            mUpperLeftPoint = points[0]?.let {
                imagePointToViewPoint(it)
            }
            mUpperRightPoint = points[1]?.let {
                imagePointToViewPoint(it)
            }
            mLowerRightPoint = points[2]?.let {
                imagePointToViewPoint(it)
            }
            mLowerLeftPoint = points[3]?.let {
                imagePointToViewPoint(it)
            }
        } else {
            setDefaultSelection()
        }
        invalidate()
    }

    /**
     * Gets the coordinates representing a rectangles corners.
     *
     * The order of the points is
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     *
     * @param rect The rectangle
     * @return An array of 8 floats
     */
    private fun getCornersFromRect(rect: RectF): FloatArray? {
        return floatArrayOf(
            rect.left, rect.top,
            rect.right, rect.top,
            rect.right, rect.bottom,
            rect.left, rect.bottom
        )
    }

    /**
     * Sets the points into a default state (A rectangle following the image view frame with
     * padding)
     */
    private fun setDefaultSelection() {
        val rect = RectF()
        val padding = 100f

        rect.right = width - padding
        rect.bottom = height - padding
        rect.top = padding
        rect.left = padding

        val pts = getCornersFromRect(rect)
        mUpperLeftPoint = PointF(pts!![0], pts[1])
        mUpperRightPoint = PointF(pts[2], pts[3])
        mLowerRightPoint = PointF(pts[4], pts[5])
        mLowerLeftPoint = PointF(pts[6], pts[7])
    }

    //----------------------------------------------isConvexQuadrilateral---------------------------------------------------
    // http://stackoverflow.com/questions/9513107/find-if-4-points-form-a-quadrilateral

    /**
     * Determine if the given points are a convex quadrilateral.  This is used to prevent the
     * selection from being dragged into an invalid state.
     *
     * @param ul The upper left point
     * @param ur The upper right point
     * @param lr The lower right point
     * @param ll The lower left point
     * @return True is the quadrilateral is convex
     */


    private fun isConvexQuadrilateral(
        ul: PointF,
        ur: PointF,
        lr: PointF,
        ll: PointF
    ): Boolean {
        val r = subtractPoints(ur, ll)
        val s = subtractPoints(ul, lr)
        val sRCrossproduct = crossProduct(r, s).toDouble()
        val t = crossProduct(subtractPoints(lr, ll), s) / sRCrossproduct
        val u = crossProduct(subtractPoints(lr, ll), r) / sRCrossproduct
        return !(t < 0 || t > 1.0 || u < 0 || u > 1.0)
    }


    private fun subtractPoints(p1: PointF, p2: PointF): PointF {
        return PointF(p1.x - p2.x, p1.y - p2.y)
    }

    private fun crossProduct(v1: PointF, v2: PointF): Float {
        return v1.x * v2.y - v1.y * v2.x
    }
}
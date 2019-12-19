package com.wachirapong.kdocscan.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.wachirapong.kdocscan.data.Quadrilateral
import kotlin.math.max
import kotlin.math.min

class RectView : View {

    private var mRectPaint: Paint? = null
    private var mDrawRect = false
    private var quadrilateral: Quadrilateral? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    /**
     * Inits internal data
     */
    private fun init() {
        mRectPaint = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            style = Paint.Style.STROKE
            strokeWidth = 5f // TODO: should take from resources
        }
    }

    fun drawRect(quadrilateral: Quadrilateral?) {
        this.quadrilateral = quadrilateral
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        quadrilateral?.points?.let {
            val path = Path().apply {
                moveTo(it[0].x.toFloat(), it[0].y.toFloat())
                lineTo(it[1].x.toFloat(), it[1].y.toFloat())
                lineTo(it[2].x.toFloat(), it[2].y.toFloat())
                lineTo(it[3].x.toFloat(), it[3].y.toFloat())
                close()
            }
            canvas.drawPath(path, mRectPaint!!)
        }
    }
}
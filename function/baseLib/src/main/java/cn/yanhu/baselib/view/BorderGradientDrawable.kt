package cn.yanhu.baselib.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * @author: zhengjun
 * created: 2023/4/23
 * desc:边框颜色渐变
 */
class BorderGradientDrawable(
    leftBorderWidth: Int,
    rightBorderWidth: Int,
    topBorderWidth: Int,
    bottomBorderWidth: Int
) : Drawable() {
    private var mLeftBorderWidth = 0
    private var mRightBorderWidth = 0
    private var mTopBorderWidth = 0
    private var mBottomBorderWidth = 0
    private var mBorderRadius = 0f
    private var mBorderRadii: FloatArray? = null
    private var mOuterPath = Path()
    private var mInnerPath = Path()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mColors: IntArray? = null
    private var mWidth: Int? = null
    private var mHeight: Int? = null
    private var mRectF: RectF? = null

    constructor(borderWidth: Int) : this(borderWidth, borderWidth, borderWidth, borderWidth)

    fun setCornerRadii(radii: FloatArray?) {
        mBorderRadii = radii
    }

    fun setCornerRadius(radius: Float) {
        mBorderRadius = radius
    }

    fun setColor(color: Int) {
        mPaint.color = color
    }

    fun setColors(colors: IntArray?) {
        mColors = colors
    }

    private var gradientType = 0
    fun setGradientStyle(type: Int) {
        gradientType = type
    }

    init {
        mLeftBorderWidth = leftBorderWidth
        mRightBorderWidth = rightBorderWidth
        mTopBorderWidth = topBorderWidth
        mBottomBorderWidth = bottomBorderWidth
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        if (mWidth == null || mHeight == null || mWidth != width || mHeight != height) {
            mOuterPath.reset()
            mInnerPath.reset()
            if (mBorderRadii != null) {
                mOuterPath.addRoundRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    mBorderRadii!!,
                    Path.Direction.CW
                )
                mInnerPath.addRoundRect(
                    mLeftBorderWidth.toFloat(),
                    mTopBorderWidth.toFloat(),
                    (width - mRightBorderWidth).toFloat(),
                    (height - mBottomBorderWidth).toFloat(),
                    mBorderRadii!!,
                    Path.Direction.CW
                )
            } else {
                mOuterPath.addRoundRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    mBorderRadius,
                    mBorderRadius,
                    Path.Direction.CW
                )
                mInnerPath.addRoundRect(
                    mLeftBorderWidth.toFloat(),
                    mTopBorderWidth.toFloat(),
                    (width - mRightBorderWidth).toFloat(),
                    (height - mBottomBorderWidth).toFloat(),
                    mBorderRadius,
                    mBorderRadius,
                    Path.Direction.CW
                )
            }
            if (null != mColors) {
                val mShader: Shader = if (gradientType == TOP_TO_BOTTOM) {
                    LinearGradient(
                        0f,
                        0f,
                        0f,
                        height.toFloat(),
                        mColors!!,
                        null,
                        Shader.TileMode.CLAMP
                    )
                } else {
                    LinearGradient(
                        0f,
                        0f,
                        width.toFloat(),
                        0f,
                        mColors!!,
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                mPaint.shader = mShader
            }
            mRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
            mWidth = width
            mHeight = height
        }
        val layerId = canvas.saveLayer(mRectF, null)
        canvas.drawPath(mOuterPath, mPaint)
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(mInnerPath, mPaint)
        mPaint.xfermode = null
        canvas.restoreToCount(layerId)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    companion object {
        const val TOP_TO_BOTTOM = 0
        const val LEFT_TO_RIGHT = 1
    }
}
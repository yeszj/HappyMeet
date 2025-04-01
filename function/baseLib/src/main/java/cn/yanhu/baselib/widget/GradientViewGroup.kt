package cn.yanhu.baselib.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import cn.yanhu.baselib.R

class GradientViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private var backgroundStartColor = Color.BLUE
    private var backgroundEndColor = Color.CYAN
    private var backgroundGradientType = 0

    private var borderWidth = 0f
    private var borderStartColor = Color.RED
    private var borderEndColor = Color.YELLOW
    private var borderGradientType = 0

    private var cornerRadius = 0f

    private val path = Path()
    private val rectF = RectF()

    init {
        setWillNotDraw(false) // 必须设置为false才能绘制

        context.withStyledAttributes(attrs, R.styleable.GradientViewGroup) {
            // 背景渐变
            backgroundStartColor =
                getColor(R.styleable.GradientViewGroup_backgroundStartColor, Color.BLUE)
            backgroundEndColor =
                getColor(R.styleable.GradientViewGroup_backgroundEndColor, Color.CYAN)
            backgroundGradientType =
                getInt(R.styleable.GradientViewGroup_backgroundGradientOrientation, 0)

            // 边框
            borderWidth = getDimension(R.styleable.GradientViewGroup_vg_borderWidth, 0f)
            borderStartColor =
                getColor(R.styleable.GradientViewGroup_vg_borderStartColor, Color.RED)
            borderEndColor = getColor(R.styleable.GradientViewGroup_vg_borderEndColor, Color.YELLOW)
            borderGradientType = getInt(R.styleable.GradientViewGroup_borderGradientOrientation, 0)

            // 圆角
            cornerRadius = getDimension(R.styleable.GradientViewGroup_cornerRadius, 0f)
        }

        borderPaint.strokeWidth = borderWidth
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGradients()
    }

    private fun updateGradients() {
        // 更新背景渐变
        backgroundPaint.shader = createGradient(
            width.toFloat(),
            height.toFloat(),
            backgroundStartColor,
            backgroundEndColor,
            backgroundGradientType
        )

        // 更新边框渐变
        if (borderWidth > 0) {
            borderPaint.shader = createGradient(
                width.toFloat(),
                height.toFloat(),
                borderStartColor,
                borderEndColor,
                borderGradientType
            )
        }
    }

    private fun createGradient(
        width: Float, height: Float, startColor: Int, endColor: Int, type: Int
    ): LinearGradient {
        return when (type) {
            0 -> LinearGradient(
                0f, 0f, measuredWidth.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP
            ) // 左右
            1 -> LinearGradient(
                0f, 0f, 0f, measuredHeight.toFloat(), startColor, endColor, Shader.TileMode.CLAMP
            ) // 上下
            2 -> LinearGradient(
                0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), startColor, endColor, Shader.TileMode.CLAMP
            ) // 对角线
            else -> LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 先测量所有子View
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var maxChildWidth = 0
        var maxChildHeight = 0

        // 计算最大子View尺寸
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                maxChildWidth = maxOf(maxChildWidth, child.measuredWidth)
                maxChildHeight = maxOf(maxChildHeight, child.measuredHeight)
            }
        }

        // 考虑padding
        val desiredWidth = maxChildWidth + paddingLeft + paddingRight + (borderWidth * 2).toInt()
        val desiredHeight = maxChildHeight + paddingTop + paddingBottom + (borderWidth * 2).toInt()

        // 考虑父容器的测量要求
        val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childLeft = paddingLeft + borderWidth.toInt()
        val childTop = paddingTop + borderWidth.toInt()
        val childRight = r - l - paddingRight - borderWidth.toInt()
        val childBottom = b - t - paddingBottom - borderWidth.toInt()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                child.layout(
                    childLeft, childTop, childRight, childBottom
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 设计时预览的特殊处理
        if (isInEditMode) {
            backgroundPaint.color = Color.LTGRAY
            canvas.drawRoundRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                cornerRadius,
                cornerRadius,
                backgroundPaint
            )

            // 绘制预览边框
            if (borderWidth > 0) {
                borderPaint.color = Color.DKGRAY
                val halfBorder = borderWidth / 2
                rectF.set(halfBorder, halfBorder, width - halfBorder, height - halfBorder)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)
            }
            return
        }

        // 绘制背景
        path.reset()
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.drawPath(path, backgroundPaint)

        // 绘制边框
        if (borderWidth > 0) {
            val halfBorder = borderWidth / 2
            rectF.set(halfBorder, halfBorder, width - halfBorder, height - halfBorder)
            path.reset()
            path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
            canvas.drawPath(path, borderPaint)
        }

        super.onDraw(canvas)
    }

    // 设置背景渐变
    fun setBackgroundGradient(startColor: Int, endColor: Int, type: Int) {
        backgroundStartColor = startColor
        backgroundEndColor = endColor
        backgroundGradientType = type
        updateGradients()
        invalidate()
    }

    // 设置边框渐变
    fun setBorderGradient(startColor: Int, endColor: Int, type: Int) {
        borderStartColor = startColor
        borderEndColor = endColor
        borderGradientType = type
        updateGradients()
        invalidate()
    }

    // 设置边框宽度
    fun setBorderWidth(width: Float) {
        borderWidth = width
        borderPaint.strokeWidth = width
        requestLayout() // 需要重新布局
        invalidate()
    }

    // 设置圆角半径
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }
}
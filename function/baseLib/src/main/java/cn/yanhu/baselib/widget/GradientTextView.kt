package cn.yanhu.baselib.widget

/**
 * @author: zhengjun
 * created: 2025/3/26
 * desc:
 */
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import cn.yanhu.baselib.R
class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

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
        context.withStyledAttributes(attrs, R.styleable.GradientTextView) {
            // 背景渐变
            backgroundStartColor = getColor(R.styleable.GradientTextView_zj_tvStartColor, Color.BLUE)
            backgroundEndColor = getColor(R.styleable.GradientTextView_zj_tvEndColor, Color.CYAN)
            backgroundGradientType = getInt(R.styleable.GradientTextView_zj_tvBgGradientOrientation, 0)

            // 边框
            borderWidth = getDimension(R.styleable.GradientTextView_zj_tvBorderWidth, 0f)
            borderStartColor = getColor(R.styleable.GradientTextView_zj_tvBorderStartColor, Color.RED)
            borderEndColor = getColor(R.styleable.GradientTextView_zj_tvBorderEndColor, Color.YELLOW)
            borderGradientType = getInt(R.styleable.GradientTextView_zj_tvBorderGradientOrientation, 0)

            // 圆角
            cornerRadius = getDimension(R.styleable.GradientViewGroup_cornerRadius, 0f)
        }

        borderPaint.strokeWidth = borderWidth
        updatePaints()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePaints()
    }

    private fun updatePaints() {
        // 更新背景渐变
        backgroundPaint.shader = createGradient(
            width.toFloat(), height.toFloat(),
            backgroundStartColor, backgroundEndColor,
            backgroundGradientType
        )

        // 更新边框渐变
        if (borderWidth > 0) {
            borderPaint.shader = createGradient(
                width.toFloat(), height.toFloat(),
                borderStartColor, borderEndColor,
                borderGradientType
            )
        }
    }

    private fun createGradient(width: Float, height: Float, startColor: Int, endColor: Int, type: Int): LinearGradient {
        return when (type) {
            0 -> LinearGradient(0f, 0f, width, 0f, startColor, endColor, Shader.TileMode.CLAMP) // 左右
            1 -> LinearGradient(0f, 0f, 0f, height, startColor, endColor, Shader.TileMode.CLAMP) // 上下
            2 -> LinearGradient(0f, 0f, width, height, startColor, endColor, Shader.TileMode.CLAMP) // 对角线
            else -> LinearGradient(0f, 0f, width, 0f, startColor, endColor, Shader.TileMode.CLAMP)
        }
    }

    override fun onDraw(canvas: Canvas) {
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

        // 绘制文本
        super.onDraw(canvas)
    }

    // 设置背景渐变
    fun setBackgroundGradient(startColor: Int, endColor: Int, type: Int) {
        backgroundStartColor = startColor
        backgroundEndColor = endColor
        backgroundGradientType = type
        updatePaints()
        invalidate()
    }

    // 设置边框渐变
    fun setBorderGradient(startColor: Int, endColor: Int, type: Int) {
        borderStartColor = startColor
        borderEndColor = endColor
        borderGradientType = type
        updatePaints()
        invalidate()
    }

    // 设置边框宽度
    fun setBorderWidth(width: Float) {
        borderWidth = width
        borderPaint.strokeWidth = width
        invalidate()
    }

    // 设置圆角半径
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }
}
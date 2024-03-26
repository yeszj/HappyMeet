package cn.yanhu.baselib.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.TextFontStyleUtils.setTextFontStyle

/**
 * @author zhengjun
 * @desc 带描边和颜色渐变的textview
 * @create at 2020/12/22 18:53
 */
class StrokeGradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CustomFontTextView(context, attrs, defStyle) {
    private val backGroundText //用于描边的TextView
            : TextView
    private var strokeColor: Int
    private var strokeEndColor: Int
    private var startColor: Int
    private var centerColor: Int
    private var endColor: Int
    private var strokeWidth: Int
    private val direction: Int

    init {
        backGroundText = TextView(context, attrs, defStyle)
        backGroundText.includeFontPadding = false
        backGroundText.textDirection = View.TEXT_DIRECTION_LOCALE
        @SuppressLint("CustomViewStyleable") val attrArray =
            context.obtainStyledAttributes(attrs, R.styleable.StrokeGradientTextView)
        strokeColor = attrArray.getColor(R.styleable.StrokeGradientTextView_strokeColor, 0)
        strokeEndColor = attrArray.getColor(R.styleable.StrokeGradientTextView_strokeEndColor, 0)
        strokeWidth =
            attrArray.getDimension(R.styleable.StrokeGradientTextView_gradientStrokeWidth, 0f).toInt()
        startColor = attrArray.getColor(R.styleable.StrokeGradientTextView_startColor, 0)
        centerColor = attrArray.getColor(R.styleable.StrokeGradientTextView_centerColor, 0)
        endColor = attrArray.getColor(R.styleable.StrokeGradientTextView_endColor, 0)
        direction = attrArray.getInt(R.styleable.StrokeGradientTextView_gradient_direction, leftToRight)
        attrArray.recycle()
    }

    fun resetStyle(strokeWidth: Int, strokeColor: Int, startColor: Int, endColor: Int) {
        this.strokeWidth = strokeWidth
        this.strokeColor = strokeColor
        this.startColor = startColor
        this.endColor = endColor
        requestLayout()
    }

    fun setStokeColor(strokeColor: Int, strokeEndColor: Int) {
        this.strokeColor = strokeColor
        this.strokeEndColor = strokeEndColor
        requestLayout()
    }

    fun setShadowColor(startColor: Int, endColor: Int) {
        this.startColor = startColor
        this.endColor = endColor
        requestLayout()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        //同步布局参数
        backGroundText.layoutParams = params
        super.setLayoutParams(params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tt = backGroundText.text
        //两个TextView上的文字必须一致
        if (tt == null || tt != this.text) {
            backGroundText.text = text
            this.postInvalidate()
        }
        backGroundText.measure(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        backGroundText.layout(left, top, right, bottom)
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            val colors: IntArray = if (centerColor == 0) {
                intArrayOf(startColor, endColor)
            } else {
                intArrayOf(startColor, centerColor, endColor)
            }
            if (direction == leftToRight) {
                val leftToRightLG = LinearGradient(
                    0f,
                    0f,
                    measuredWidth.toFloat(),
                    0f,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = leftToRightLG
            } else {
                val topToBottomLG = LinearGradient(
                    0f,
                    0f,
                    0f,
                    measuredHeight.toFloat(),
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                ) //从上到下渐变
                paint.shader = topToBottomLG
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        //其他地方，backGroundText和super的先后顺序影响不会很大，但是此处必须要先绘制backGroundText，
        init()
        backGroundText.draw(canvas)
        super.onDraw(canvas)
    }

    fun setStrokeText(text: CharSequence?) {
        backGroundText.text = String.format("%s ", text)
        setText(String.format("%s ", text))
    }

    fun init() {
        val tp1 = backGroundText.paint

        //设置描边宽度
        tp1.strokeWidth = strokeWidth.toFloat()

        //背景描边并填充全部
        setTextFontStyle(backGroundText, fontType, fontStyle)
        tp1.style = Paint.Style.FILL_AND_STROKE
        if (strokeEndColor == 0) {
            //设置描边颜色
            backGroundText.setTextColor(strokeColor)
        } else {
            val topToBottomLG = LinearGradient(
                0f,
                0f,
                0f,
                measuredHeight.toFloat(),
                strokeColor,
                strokeEndColor,
                Shader.TileMode.CLAMP
            ) //从上到下渐变
            tp1.shader = topToBottomLG
        }
        //将背景的文字对齐方式做同步
        backGroundText.gravity = gravity
    }

    companion object {
        const val leftToRight = 1
    }
}
package cn.yanhu.commonres.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.yanhu.commonres.R
import androidx.core.content.withStyledAttributes

class CustomMarqueeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var text: String = ""
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textWidth = 0f
    private var offsetX = 0f
    private var speed = 2f
    private var isRunning = true
    private var spacing = 50f
    private var viewWidth = 0

    init {
        paint.color = -0x1000000
        paint.textSize = 48f

        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomMarqueeTextView) {
                text = getString(R.styleable.CustomMarqueeTextView_mq_text) ?: ""
                speed = getFloat(R.styleable.CustomMarqueeTextView_mq_speed, 2f)
                paint.color = getColor(R.styleable.CustomMarqueeTextView_mq_textColor, -0x1000000)
                paint.textSize = getDimension(R.styleable.CustomMarqueeTextView_mq_textSize, 48f)
                spacing = getDimension(R.styleable.CustomMarqueeTextView_mq_spacing, 50f)
            }
        }
        measureText()
    }

    fun setTextColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setText(text: String?) {
        this.text = text ?: ""
        measureText()
        offsetX = 0f
        invalidate()
    }

    fun setSpeed(speed: Float) {
        this.speed = speed
    }

    fun setSpacing(spacing: Float) {
        this.spacing = spacing
        invalidate()
    }

    fun startScrolling() {
        if (!isRunning) {
            isRunning = true
            invalidate()
        }
    }

    fun stopScrolling() {
        isRunning = false
    }

    private fun measureText() {
        textWidth = if (text.isNotEmpty()) {
            paint.measureText(text)
        } else {
            0f
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        measureText()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (text.isEmpty() || textWidth == 0f) return

        val y = (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

        if (textWidth > width) {
            // 需要滚动的情况
            canvas.drawText(text, -offsetX, y, paint)
            canvas.drawText(text, -offsetX + textWidth + spacing, y, paint)

            if (isRunning) {
                offsetX += speed
                if (offsetX >= textWidth + spacing) {
                    offsetX = 0f
                }
                postInvalidateOnAnimation()
            }
        } else {
            // 不需要滚动，靠左显示
            canvas.drawText(text, 0f, y, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRunning = true
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isRunning = false
    }
}
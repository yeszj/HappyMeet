package cn.yanhu.baselib.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.TextFontStyleUtils.setTextFontStyle
import com.blankj.utilcode.util.StringUtils
import androidx.core.content.withStyledAttributes

/**
 * @author: witness
 * created: 2022/6/6
 * desc:
 */
open class CustomFontEditText : AppCompatEditText {
    private var fontType: String = StringUtils.getString(R.string.fontRegular)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(
            attrs,
            R.styleable.CustomFontTextView
        ) {
            fontType = getString(R.styleable.CustomFontTextView_fontType).toString()
            setTextFontStyle(this@CustomFontEditText, fontType)
            includeFontPadding = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_UP) {
            val drawable = getCompoundDrawables()[2]
            if (drawable != null && event.x <= (width - getPaddingRight()) && event.x >= (width - getPaddingRight() - drawable.getBounds()
                    .width())
            ) {
                clickRightDrawableListener?.clickRightDrawable()
            }
        }
        return super.onTouchEvent(event)
    }

    var clickRightDrawableListener: OnClickRightDrawableListener? = null
    interface OnClickRightDrawableListener{
        fun clickRightDrawable()
    }
}
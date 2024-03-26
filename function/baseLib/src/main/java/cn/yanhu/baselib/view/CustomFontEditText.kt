package cn.yanhu.baselib.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.TextFontStyleUtils.setTextFontStyle
import com.blankj.utilcode.util.StringUtils

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
        @SuppressLint("Recycle", "CustomViewStyleable") val typedArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomFontTextView
            )
        fontType = typedArray.getString(R.styleable.CustomFontTextView_fontType).toString()
        setTextFontStyle(this, fontType)
        includeFontPadding = false
        typedArray.recycle()
    }
}
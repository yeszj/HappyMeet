package cn.yanhu.baselib.view

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.TextFontStyleUtils.setTextFontStyle

/**
 * @author: witness
 * created: 2022/6/6
 * desc:
 */
open class CustomFontTextView : AppCompatTextView {
    var fontType: String? = "regular"
    var fontStyle = 0

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val typedArray = context.resources.obtainAttributes(
            attrs,
            R.styleable.CustomFontTextView
        )
        fontType = typedArray.getString(R.styleable.CustomFontTextView_fontType)
        fontStyle =
            typedArray.getInt(R.styleable.CustomFontTextView_fontCustomStyle, Typeface.NORMAL)
        if (TextUtils.isEmpty(fontType)) {
            fontType = "regular"
        }
        setTextFontStyle(this, fontType, fontStyle)
        includeFontPadding = false
        typedArray.recycle()
    }
}
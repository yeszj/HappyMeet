package cn.yanhu.baselib.widget

/**
 * @author: zhengjun
 * created: 2025/3/31
 * desc:
 */

import android.content.Context
import android.util.AttributeSet
import cn.yanhu.baselib.R

open class FontWeightValueTextView(
    context: Context,
    attrs: AttributeSet? = null
) : FontWeightTextView(context, attrs) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FontWeightValueTextView).run {
            if (hasValue(R.styleable.FontWeightValueTextView_fontWeightValue)) {
                val fontWeightValue = getInt(R.styleable.FontWeightValueTextView_fontWeightValue, 400)
                setFontWeight(fontWeightValue)
            }
            recycle()
        }
    }
}
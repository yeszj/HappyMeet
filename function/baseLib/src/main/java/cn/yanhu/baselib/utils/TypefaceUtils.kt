package cn.yanhu.baselib.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import cn.yanhu.baselib.R
/**
 * @author: zhengjun
 * created: 2025/7/28
 * desc:
 */
object TypefaceUtils {
    fun getTypeface(weight: Int,context: Context,isItalic: Boolean = false): Typeface {
        return when (weight) {
            100,200,300 -> Typeface.create("sans-serif-light", if (isItalic) Typeface.ITALIC else Typeface.NORMAL)
            500 -> ResourcesCompat.getFont(context, R.font.misans_medium)
            700 -> Typeface.create("sans-serif",if (isItalic) Typeface.ITALIC else  Typeface.BOLD)
            900 -> Typeface.create("sans-serif-black",if (isItalic) Typeface.ITALIC else  Typeface.NORMAL)
            else -> Typeface.DEFAULT
        }!!
    }
}
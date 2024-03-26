package cn.yanhu.baselib.utils.ext

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * 检测目标字符串是否为空
 * @receiver String
 * @param condition 条件
 * @param suc 不为空执行的代码块
 * @param fail 为空执行的代码块
 * @return  是否为空
 */
fun String.isValide(
    condition: () -> Boolean,
    suc: () -> Unit = {},
    fail: () -> Unit = {}
): Boolean {
    return if (condition.invoke()) {
        suc.invoke()
        true
    } else {
        fail.invoke()
        false
    }
}

fun isCustomEmpty(value: String): Boolean {
    return value.isBlank()
}

/**
 * 显示html
 * @receiver String
 * @return Spanned
 */
fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY, null, null)
    } else {
        Html.fromHtml(this)
    }
}
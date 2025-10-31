package cn.yanhu.baselib.utils

import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.graphics.toColorInt

/**
 * @author: zhengjun
 * created: 2025/10/20
 * desc:
 */
/**
 * 处理 HTML 文本中的特定关键词点击
 */
object HtmlClickProcessor {

    fun setHtmlTextWithClick(
        textView: TextView,
        htmlText: String,
        clickableWord: String,
        onClick: () -> Unit
    ) {
        // 将HTML内容解析为SpannableString
        val spanned = SpannableString(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY))

        // 为"举报"两个字添加点击事件，并设置红色
        setClickableSpanForWord(spanned, clickableWord, onClick)

        // 设置TextView以响应点击事件
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = spanned
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun setClickableSpanForWord(
        spannable: SpannableString,
        word: String,
        onClick: () -> Unit
    ) {
        val text = spannable.toString()
        var startIndex = 0

        while (startIndex < text.length) {
            val index = text.indexOf(word, startIndex)
            if (index == -1) break

            val endIndex = index + word.length

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick.invoke() // 执行点击事件
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    // 保持点击文字的红色
                    ds.color = "#E83D24".toColorInt() // 设置红色
                    ds.isUnderlineText = false // 去除下划线
                }
            }

            // 设置 "举报" 字的点击事件
            spannable.setSpan(
                clickableSpan,
                index,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // 设置 "举报" 字的颜色
            spannable.setSpan(
                ForegroundColorSpan("#E83D24".toColorInt()),
                index,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            startIndex = endIndex
        }
    }



}
package cn.yanhu.baselib.widget.spans

import android.content.Context
import android.text.style.ClickableSpan
import android.text.TextPaint
import android.view.View
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.R

/**
 * @author zhengjun
 * @desc
 * @create at 2020/6/15 16:00
 */
open class CustomClickSpan : ClickableSpan {
    private val clickListener: OnAllSpanClickListener?
    private var isPressed = false
    private val context: Context
    private var colorId = -1

    constructor(context: Context, clickListener: OnAllSpanClickListener?) {
        this.context = context
        this.clickListener = clickListener
    }

    constructor(context: Context, colorId: Int, clickListener: OnAllSpanClickListener?) {
        this.context = context
        this.colorId = colorId
        this.clickListener = clickListener
    }

    override fun onClick(widget: View) {
        clickListener?.onClick(widget)
    }

    fun setPressed(pressed: Boolean) {
        isPressed = pressed
    }

    interface OnAllSpanClickListener {
        fun onClick(widget: View?)
    }

    override fun updateDrawState(ds: TextPaint) {
        if (isPressed) {
            ds.bgColor = ContextCompat.getColor(context,android.R.color.darker_gray)
        } else {
            ds.bgColor = ContextCompat.getColor(context,android.R.color.transparent)
        }
        if (colorId != -1) {
            ds.color = colorId
        } else {
            ds.color = ContextCompat.getColor(context, R.color.colorPrimary)
        }
        ds.isUnderlineText = false
    }
}
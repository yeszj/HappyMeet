package cn.yanhu.baselib.widget.indicator

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import androidx.appcompat.widget.AppCompatTextView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IMeasurablePagerTitleView
import android.view.Gravity
import net.lucode.hackware.magicindicator.buildins.UIUtil

/**
 * @author: witness
 * created: 2022/5/11
 * desc:
 */
internal class BgPagerTitleView(context: Context) : AppCompatTextView(context, null),
    IMeasurablePagerTitleView {
    var mSelectedBg = 0
    var mNormalBg = 0
    var selectedColor = 0
    var normalColor = 0
    private fun init(context: Context) {
        gravity = Gravity.CENTER
        val padding = UIUtil.dip2px(context, 10.0)
        setPadding(padding, 0, padding, 0)
        setSingleLine()
        ellipsize = TextUtils.TruncateAt.END
    }

    override fun onSelected(index: Int, totalCount: Int) {
        setTextColor(selectedColor)
        setBackgroundResource(mSelectedBg)
    }

    override fun onDeselected(index: Int, totalCount: Int) {
        setTextColor(normalColor)
        setBackgroundResource(mNormalBg)
    }

    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {}
    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {}
    override fun getContentLeft(): Int {
        val bound = Rect()
        var longestString = ""
        if (text.toString().contains("\n")) {
            val brokenStrings = text.toString().split("\\n".toRegex()).toTypedArray()
            for (each in brokenStrings) {
                if (each.length > longestString.length) longestString = each
            }
        } else {
            longestString = text.toString()
        }
        paint.getTextBounds(longestString, 0, longestString.length, bound)
        val contentWidth = bound.width()
        return left + width / 2 - contentWidth / 2
    }

    override fun getContentTop(): Int {
        val metrics = paint.fontMetrics
        val contentHeight = metrics.bottom - metrics.top
        return (height / 2 - contentHeight / 2).toInt()
    }

    override fun getContentRight(): Int {
        val bound = Rect()
        var longestString = ""
        if (text.toString().contains("\n")) {
            val brokenStrings = text.toString().split("\\n".toRegex()).toTypedArray()
            for (each in brokenStrings) {
                if (each.length > longestString.length) longestString = each
            }
        } else {
            longestString = text.toString()
        }
        paint.getTextBounds(longestString, 0, longestString.length, bound)
        val contentWidth = bound.width()
        return left + width / 2 + contentWidth / 2
    }

    override fun getContentBottom(): Int {
        val metrics = paint.fontMetrics
        val contentHeight = metrics.bottom - metrics.top
        return (height / 2 + contentHeight / 2).toInt()
    }

    init {
        init(context)
    }
}
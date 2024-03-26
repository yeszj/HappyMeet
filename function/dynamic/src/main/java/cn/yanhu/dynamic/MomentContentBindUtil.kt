package cn.yanhu.dynamic

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.SpansManager
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2023/7/3
 * desc:
 */
object MomentContentBindUtil {

    interface OnClickTextListener {
        fun onClickText()
    }

    @SuppressLint("SetTextI18n")
    fun setTextEndTextSpan(
        textView: TextView,
        text: String,
        tvMore: TextView,
        user: BaseUserInfo? = null,
        clickTextListener: OnClickTextListener?=null,
    ) {
        val stringBuilder = getContentStringBuilder(user, text)
        textView.text = stringBuilder
        val layout = textView.layout
        if (layout == null) {
            textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setTextSpan(
                        textView,
                        text,
                        tvMore,
                        user
                    )
                }
            })
        } else {
            setTextSpan(
                textView,
                text,
                tvMore,
                user
            )
        }
    }

    private fun getContentStringBuilder(
        user: BaseUserInfo?,
        text: String,
    ): SpannableStringBuilder {
        val stringBuilder = SpannableStringBuilder()
        if (user != null) {
            stringBuilder
                .append(
                    SpansManager.getSpan(
                        "@${user.nickName} ",
                        com.zj.dimens.R.dimen.sp_15,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.manColor)
                    )
                )
        }
        stringBuilder.append(
            SpansManager.getSpan(
                text,
                com.zj.dimens. R.dimen.sp_15,
                CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor)
            )
        )
        return stringBuilder
    }


    /**
     * 执行设置span
     */
    private fun setTextSpan(
        textView: TextView,
        text: String,
        tvMore: TextView,
        user: BaseUserInfo? = null,
    ) {
        val layout = textView.layout ?: return
        if (layout.lineCount > 0) {
            val line = textView.lineCount - 1 //最后一行
            val count = layout.getEllipsisCount(line) //省略的文字数
            if (count > 0) { //文字过长，有省略的文字
                val contentStringBuilder = getContentStringBuilder(user, text)
                textView.text = contentStringBuilder
                tvMore.visibility = View.VISIBLE
                addEndText(
                    tvMore,
                    textView,
                )
            } else {
                tvMore.visibility = View.GONE
            }
        }
    }

    private fun addEndText(
        tvMore: TextView,
        textView: TextView,
    ) {
        tvMore.tag = true
        val maxLine = textView.maxLines
        tvMore.setOnClickListener {
            val isHide = tvMore.tag as Boolean
            if (isHide) {
                textView.maxLines = Int.MAX_VALUE
                tvMore.text = "隐藏"
                TextViewDrawableUtils.setDrawableRight(
                    tvMore,
                    ContextCompat.getDrawable(
                        ActivityUtils.getTopActivity(),
                        R.drawable.icon_hidemore
                    )
                )
              //展示全文
                tvMore.tag = false
            } else {
                textView.maxLines = maxLine//恢复初始
                tvMore.text ="更多"
                TextViewDrawableUtils.setDrawableRight(
                    tvMore,
                    ContextCompat.getDrawable(
                        ActivityUtils.getTopActivity(),
                        R.drawable.icon_seemore
                    )
                )
                tvMore.tag = true
            }
        }
    }
}
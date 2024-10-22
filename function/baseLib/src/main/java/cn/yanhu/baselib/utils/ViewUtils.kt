package cn.yanhu.baselib.utils

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider

/**
 * @author zhengjun
 * @desc 工具类
 * @create at 2018/8/14 11:40
 */
object ViewUtils {
    fun setViewCorner(view: View, radius: Int) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                val leftMargin = 0
                val topMargin = 0
                val selfRect = Rect(
                    leftMargin, topMargin,
                    rect.right - rect.left - leftMargin,
                    rect.bottom - rect.top - topMargin
                )
                outline.setRoundRect(selfRect, radius.toFloat())
            }
        }
        view.clipToOutline = true
    }

    fun setMarginLeftAndTop(view: View, leftMargin: Int, topMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = leftMargin
            layoutParams.topMargin = topMargin
            view.layoutParams = layoutParams
        }
    }

    fun setMarginLeft(view: View, leftMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = leftMargin
            view.layoutParams = layoutParams
        }
    }

    fun setMarginRight(view: View, rightMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.rightMargin = rightMargin
            view.layoutParams = layoutParams
        }
    }

    fun setMarginTop(view: View, topMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.topMargin = topMargin
            view.layoutParams = layoutParams
        }
    }

    fun setViewWidth(view: View, width: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = width
            view.layoutParams = layoutParams
        }
    }

    fun setViewSize(view: View?, width: Int, height: Int) {
        if (view==null){
            return
        }
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = width
            layoutParams.height = height
            view.layoutParams = layoutParams
        }
    }

    fun setViewHeight(view: View, height: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.height = height
            view.layoutParams = layoutParams
        }
    }

    fun setViewWidthMatch(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = layoutParams
        }
    }

    fun setViewWidthWrap(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            view.layoutParams = layoutParams
        }
    }

    fun setViewHeightWrap(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            view.layoutParams = layoutParams
        }
    }

    fun setViewHeightMatch(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = layoutParams
        }
    }

    fun setViewWrap(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            view.layoutParams = layoutParams
        }
    }

    fun setViewMatch(view: View) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = layoutParams
        }
    }

    fun setMarginBottom(view: View, bottomMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.bottomMargin = bottomMargin
            view.layoutParams = layoutParams
        }
    }

    /**
     * 将View从父控件中移除
     */
    fun removeViewFormParent(v: View?) {
        if (v == null) return
        val parent = v.parent
        if (parent is ViewGroup) {
            parent.removeView(v)
        }
    }

    fun setPaddingTop(view: View?, topMargin: Int) {
        view?.setPadding(view.paddingLeft, topMargin, view.paddingRight, view.paddingBottom)
    }

    fun setPaddingLeft(view: View?, leftMargin: Int) {
        view?.setPadding(leftMargin, 0, 0, 0)
    }

    fun setPaddingRight(view: View?, rightMargin: Int) {
        view?.setPadding(0, 0, rightMargin, 0)
    }

    fun setPaddingHorizontal(view: View?, leftMargin: Int) {
        view?.setPadding(leftMargin, 0, leftMargin, 0)
    }

    fun setPaddingVertical(view: View?, padding: Int) {
        view?.setPadding(0, padding, 0, padding)
    }

    fun setPaddingBottom(view: View?, padding: Int) {
        view?.setPadding(0, 0, 0, padding)
    }

    fun setPaddingRightAndBottom(view: View?, padding: Int) {
        view?.setPadding(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18), 0, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18), padding)
    }

    fun setViewPadding(view: View?, padding: Int) {
        view?.setPadding(padding, padding, padding, padding)
    }

    fun setViewPadding(view: View?, horizontalPadding: Int,verticalPadding: Int) {
        view?.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }
}
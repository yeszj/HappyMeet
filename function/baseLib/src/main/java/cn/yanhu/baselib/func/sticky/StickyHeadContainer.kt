package cn.yanhu.baselib.func.sticky

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.ViewCompat

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class StickyHeadContainer @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var mOffset = 0
    private var mLastOffset = Int.MIN_VALUE
    private var mLastStickyHeadPosition = Int.MIN_VALUE
    private var mLeft = 0
    private var mRight = 0
    private var mTop = 0
    private var mBottom = 0
    private var mDataCallback: DataCallback? = null

    init {
        setOnClickListener {
            // TODO: 2017/1/9 屏蔽点击事件
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var desireHeight: Int
        var desireWidth: Int
        val count = childCount
        require(count == 1) { "只允许容器添加1个子View！" }
        val child = getChildAt(0)
        // 测量子元素并考虑外边距
        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
        // 获取子元素的布局参数
        val lp = child.layoutParams as MarginLayoutParams
        // 计算子元素宽度，取子控件最大宽度
        desireWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
        // 计算子元素高度
        desireHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

        // 考虑父容器内边距
        desireWidth += paddingLeft + paddingRight
        desireHeight += paddingTop + paddingBottom
        // 尝试比较建议最小值和期望值的大小并取大值
        desireWidth = Math.max(desireWidth, suggestedMinimumWidth)
        desireHeight = Math.max(desireHeight, suggestedMinimumHeight)
        // 设置最终测量值
        setMeasuredDimension(
            resolveSize(desireWidth, widthMeasureSpec),
            resolveSize(desireHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = getChildAt(0)
        val lp = child.layoutParams as MarginLayoutParams
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        mLeft = paddingLeft + lp.leftMargin
        mRight = child.measuredWidth + mLeft
        mTop = paddingTop + lp.topMargin + mOffset
        mBottom = child.measuredHeight + mTop
        child.layout(mLeft, mTop, mRight, mBottom)
    }

    // 生成默认的布局参数
    override fun generateDefaultLayoutParams(): LayoutParams {
        return super.generateDefaultLayoutParams()
    }

    // 生成布局参数,将布局参数包装成我们的
    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    // 生成布局参数,从属性配置中生成我们的布局参数
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    // 查当前布局参数是否是我们定义的类型这在code声明布局参数时常常用到
    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    fun scrollChild(offset: Int) {
        if (mLastOffset != offset) {
            mOffset = offset
            ViewCompat.offsetTopAndBottom(getChildAt(0), mOffset - mLastOffset)
        }
        mLastOffset = mOffset
    }

    val childHeight: Int
        get() = getChildAt(0).height

    fun onDataChange(stickyHeadPosition: Int) {
        if (mDataCallback != null && mLastStickyHeadPosition != stickyHeadPosition) {
            mDataCallback!!.onDataChange(stickyHeadPosition)
        }
        mLastStickyHeadPosition = stickyHeadPosition
    }

    fun reset() {
        mLastStickyHeadPosition = Int.MIN_VALUE
    }

    interface DataCallback {
        fun onDataChange(pos: Int)
    }

    fun setDataCallback(dataCallback: DataCallback?) {
        mDataCallback = dataCallback
    }
}
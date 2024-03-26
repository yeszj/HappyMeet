package cn.yanhu.baselib.func.sticky

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
interface OnStickyChangeListener {
    fun onScrollable(offset: Int)
    fun onInVisible()
}
package cn.yanhu.baselib.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.reflect.Field

/**
 * @author: zhengjun
 * created: 2024/2/19
 * desc:
 */
object ViewPagerUtil {
    /**
     * 降级ViewPager2灵敏度
     */
    fun ViewPager2.desensitization(){
        //动态设置ViewPager2 灵敏度
        try {
            val recyclerViewField: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(this) as RecyclerView
            val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int
            touchSlopField.set(recyclerView, touchSlop * 1) //6 is empirical value
        } catch (ignore: java.lang.Exception) {
        }
    }
}
package cn.yanhu.baselib.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * @author: zhengjun
 * created: 2025/2/12
 * desc:
 */
 class CustomViewPagerAdapter    //有参构造
    (private val views: List<View>) : PagerAdapter() {
    //获得长度
    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    //展示的view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        //获得展示的view
        val view = views[position]
        //添加到容器
        container.addView(view)
        //返回显示的view
        return view
    }

    //销毁view
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        //从容器中移除view
        container.removeView(`object` as View)
    }
}
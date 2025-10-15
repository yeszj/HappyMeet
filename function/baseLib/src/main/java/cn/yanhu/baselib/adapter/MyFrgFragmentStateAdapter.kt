package cn.yanhu.baselib.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
 class MyFrgFragmentStateAdapter(fragmentActivity: Fragment, private val  list: MutableList<Fragment>) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    // 移除单个项目
    fun removeItem(position: Int) {
        if (position < 0 || position >= list.size) return

        list.removeAt(position)
        notifyItemRemoved(position)

        // 如果移除后还有数据，需要通知后续项目位置变化
        if (position < list.size) {
            notifyItemRangeChanged(position, list.size - position)
        }
    }
}
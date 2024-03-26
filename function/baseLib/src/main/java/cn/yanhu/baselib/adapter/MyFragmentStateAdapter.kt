package cn.yanhu.baselib.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
 class MyFragmentStateAdapter(fragmentActivity: FragmentActivity,private val  list: MutableList<Fragment>) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
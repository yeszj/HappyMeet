package cn.yanhu.baselib.base

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * @author: witness
 * created: 2022/3/23
 * desc:
 */
open class BaseTabAdapter(
    private val fragmentManager: FragmentManager,
    private val titleList: MutableList<String>,
    private val fragmentList: MutableList<Fragment>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ) {

    override fun getCount(): Int {
        return fragmentList.size
    }


    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss()
        return super.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = fragmentList[position]
        fragmentManager.beginTransaction().hide(fragment).commitAllowingStateLoss()
    }
}
package cn.huanyuan.sweetlove.ui.main.tab_blinddate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgBlindDateUserListBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:交友
 */
class BlindDateUserRoomListFrg : BaseFragment<FrgBlindDateUserListBinding, MainViewModel>(
    R.layout.frg_blind_date_user_list,
    MainViewModel::class.java
) {
    private val titles = mutableListOf<String>()
    private val frgList: MutableList<Fragment> = ArrayList()

    override fun initData() {
        initVpData()
    }

    private fun initVpData() {
        if (frgList.size > 0) {
            clearAllFrgManager()
        }
        titles.clear()
        titles.add("推荐")
        titles.add("专属")
        frgList.add(BlindUserOrRoomItemFrg.newsInstance(BlindUserOrRoomItemFrg.TYPE_RECOMMEND))
        frgList.add(BlindUserOrRoomItemFrg.newsInstance(BlindUserOrRoomItemFrg.TYPE_EXCLUSIVE))
        mBinding.apply {
            viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
            viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            viewPager.isSaveEnabled = false
            tabLayout.initViewPager(viewPager, titles.toMutableList(), -1)
        }
    }

    private fun clearAllFrgManager() {
        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
        if (frgList.size > 0) {
            for (fragment in frgList) {
                ft.remove(fragment)
            }
            ft.commitNow()
            frgList.clear()
            mBinding.tabLayout.removeAllViews()
        }
    }
}
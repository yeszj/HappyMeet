package cn.huanyuan.happymeet.ui.main.tab_blinddate

import androidx.fragment.app.Fragment
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgTabBlinddateBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.task.TaskCenterActivity
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.commonres.router.RouteIntent
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class TabBlindDateFrg : BaseFragment<FrgTabBlinddateBinding, MainViewModel>(
    R.layout.frg_tab_blinddate,
    MainViewModel::class.java
) {
    override fun initData() {
        //mBinding.viewPager.desensitization()
        initTabLayout()
        initVpData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvStart.setOnSingleClickListener { RouteIntent.lunchToCreateRoom() }
        mBinding.ivGift.setOnSingleClickListener { TaskCenterActivity.lunch(mContext) }
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("相亲", "交友")
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.viewPager,
            list.toTypedArray(),
            textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_18).toFloat(),
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8)
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.viewPager)
    }

    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initVpData() {
        frgList.add(BlindDateUserRoomListFrg())

        frgList.add(BlindUserOrRoomItemFrg.newsInstance(BlindUserOrRoomItemFrg.TYPE_FRIENDS))

        mBinding.viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
    }

}
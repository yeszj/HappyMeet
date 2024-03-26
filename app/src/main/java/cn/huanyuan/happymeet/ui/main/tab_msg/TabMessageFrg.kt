package cn.huanyuan.happymeet.ui.main.tab_msg

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgTabMessageBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.imchat.conversation.ConversationFrg
import cn.yanhu.imchat.conversation.GroupConversationFrg
import cn.yanhu.imchat.pop.CreateGroupPop
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class TabMessageFrg : BaseFragment<FrgTabMessageBinding, MainViewModel>(
    R.layout.frg_tab_message,
    MainViewModel::class.java
) {
    override fun initData() {
        initTabLayout()
        initVpData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivCreate.setOnSingleClickListener {
            CreateGroupPop.showDialog(mContext)
        }
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("消息", "好友","群聊")
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
        frgList.add(ConversationFrg())

        frgList.add(FriendsFrg())

        frgList.add(GroupConversationFrg())
        mBinding.viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position==2){
                    mBinding.ivCreate.visibility = View.VISIBLE
                }else{
                    mBinding.ivCreate.visibility = View.INVISIBLE
                }
            }
        })
    }
}
package cn.huanyuan.happymeet.ui.userinfo.dressUp

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityDressUpBinding
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:我的装扮
 */
class MyDressUpActivity : BaseActivity<ActivityDressUpBinding, DressUpViewModel>(
    R.layout.activity_dress_up,
    DressUpViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar()
        mBinding.titleBar.setLeftTitleName("我的装扮")
        mBinding.titleBar.setTitleRightText("装扮中心")
        initTabLayout()
        initVpData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                finish()
            }
            override fun rightButtonOnClick(v: View?) {
                DressUpCenterActivity.lunch(mContext)
                finish()
            }
        })
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("头像框", "座驾","聊天气泡","靓号")
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.viewPager,
            list.toTypedArray(),
            textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_16).toFloat(),
            selectColor = R.color.white,
            normalColor = cn.yanhu.baselib.R.color.whiteAlpha20,
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.viewPager)
    }

    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initVpData() {
        frgList.add(MyDressUpFrg.newInstance(MyDressUpFrg.TYPE_AVATAR_FRAME))
        frgList.add(MyDressUpFrg.newInstance(MyDressUpFrg.TYPE_CAR))
        frgList.add(MyDressUpFrg.newInstance(MyDressUpFrg.TYPE_CHAT_POP))
        frgList.add(MyDressUpFrg.newInstance(MyDressUpFrg.TYPE_BEAUTIFUL_ACCOUNT))
        mBinding.viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context,MyDressUpActivity::class.java))
        }
    }
}
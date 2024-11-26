package cn.huanyuan.sweetlove.ui.userinfo.dressUp

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityDressUpBinding
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.commonres.bean.TabConfigInfo
import cn.zj.netrequest.ext.parseState
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:装扮中心
 */
class DressUpCenterActivity : BaseActivity<ActivityDressUpBinding, DressUpViewModel>(
    R.layout.activity_dress_up,
    DressUpViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar()
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getStoreTabs()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                finish()
            }
            override fun rightButtonOnClick(v: View?) {
                MyDressUpActivity.lunch(mContext)
            }
        })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.tabListObservable.observe(this){ it ->
            parseState(it,{
                if (frgList.size<=0){
                    initTabLayout(it)
                }
            })
        }
    }

    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initTabLayout(tabList:List<TabConfigInfo>) {
        val list = mutableListOf<String>()
        tabList.forEach {
            list.add(it.name)
            frgList.add(DressUpFrg.newInstance(it.id))
        }
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
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

        mBinding.viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size

    }


    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context,DressUpCenterActivity::class.java))
        }
    }
}
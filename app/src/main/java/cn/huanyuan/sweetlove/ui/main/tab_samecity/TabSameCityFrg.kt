package cn.huanyuan.sweetlove.ui.main.tab_samecity

import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgTabSameCityBinding
import cn.huanyuan.sweetlove.func.dialog.UserFilterPop
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.yanhu.baselib.adapter.MyFrgFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.baselib.widget.indicator.CommonIndicatorTitleView
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.sdk.location.LocationUtils
import com.tencent.map.geolocation.TencentLocation
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import org.json.JSONObject

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class TabSameCityFrg : BaseFragment<FrgTabSameCityBinding, MainViewModel>(
    R.layout.frg_tab_same_city, MainViewModel::class.java
) {
    private lateinit var commonNavigator: CommonNavigator
    private var isGetLocation: Boolean = false
    private var filterCity: String = ""
    private var filterAge: String = ""
    override fun initData() {
        initTabLayout()
        initVpData()
       // getLocation()
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvFilter.setOnSingleClickListener {
            showFilterPop()
        }
    }

    private var userFilterPop: UserFilterPop? = null
    private fun showFilterPop() {
        if (userFilterPop == null) {
            filterCity = AppCacheManager.province
            userFilterPop = UserFilterPop.showDialog(
                mContext,
                filterCity,
                filterAge,
                object : UserFilterPop.OnFilterValueListener {
                    override fun onFilterValue(province: String, age: String) {
                        filterCity = province
                        filterAge = age
                        if (TextUtils.isEmpty(filterCity)) {
                            setLocationTitleValue("不限")
                        } else {
                            setLocationTitleValue(filterCity)
                        }
                        refreshUserList()
                    }
                })
        } else {
            userFilterPop?.show()
        }
    }

    private fun refreshUserList() {
        val jsonObject = JSONObject()
        jsonObject.put(UserListFrg.FILTER_CITY, filterCity)
        jsonObject.put(UserListFrg.FILTER_AGE, filterAge)
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.START_FILTER_USER, jsonObject)
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf(
            if (!TextUtils.isEmpty(AppCacheManager.province)) AppCacheManager.province else "缘分"
        )
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

    private var frgList: MutableList<Fragment> = mutableListOf()
    private fun initVpData() {
        DialogUtils
        val fragments = childFragmentManager.fragments
        if (fragments.size>0){
            frgList = fragments
        }else{
            frgList.add(UserListFrg())
        }

        //   frgList.add(DynamicListFrg())

        mBinding.viewPager.adapter = MyFrgFragmentStateAdapter(this@TabSameCityFrg, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
//                if (position == 0) {
//                    mBinding.tvFilter.visibility = View.VISIBLE
//                    mBinding.vgDynamicCount.visibility = View.INVISIBLE
//                } else {
//                    mBinding.tvFilter.visibility = View.INVISIBLE
//                    mBinding.vgDynamicCount.visibility = View.VISIBLE
//                }
            }
        })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
//        LiveEventBus.get<Int>(LiveDataEventManager.SWITCH_TO_SAME_CITY).observe(this) {
//            getLocation()
//        }
        LiveEventBus.get<String>(LiveDataEventManager.REFRESH_SAMECITY_TAB).observe(this) {
            refreshLocationTab(it)
        }
    }

    private fun getLocation() {
        if (!isGetLocation && TextUtils.isEmpty(AppCacheManager.province)) {
            isGetLocation = true
            LocationUtils.getTencentLocation(
                this@TabSameCityFrg,
                object : LocationUtils.OnLocationResultListener {
                    override fun onLocationResult(aMapLocation: TencentLocation?) {
                        val province = aMapLocation?.province
                        refreshLocationTab(province)
                    }

                })
        }
    }

    private fun refreshLocationTab(province: String?) {
        if (!TextUtils.isEmpty(province)) {
            setLocationTitleValue(province!!)
            filterCity = province
        }
        refreshUserList()
    }

    private fun setLocationTitleValue(province: String) {
        val pagerTitleView =
            commonNavigator.getPagerTitleView(0) as CommonIndicatorTitleView
        pagerTitleView.text = province

    }
}
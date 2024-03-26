package cn.huanyuan.happymeet.ui.main.tab_samecity

import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgTabSameCityBinding
import cn.huanyuan.happymeet.func.dialog.UserFilterPop
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.baselib.widget.indicator.CommonIndicatorTitleView
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.dynamic.ui.frg.DynamicListFrg
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.sdk.location.LocationCacheManager
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
    private var filterCity:String = ""
    private var filterAge:String = ""
    override fun initData() {
        initTabLayout()
        initVpData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvFilter.setOnSingleClickListener {
            showFilterPop()
        }
    }

    private var userFilterPop:UserFilterPop?=null
    private fun showFilterPop(){
        if (userFilterPop==null){
            filterCity = LocationCacheManager.mapLocation?.province.toString()
            userFilterPop = UserFilterPop.showDialog(
                mContext,
                filterCity,
                filterAge,
                object : UserFilterPop.OnFilterValueListener {
                    override fun onFilterValue(province: String, age: String) {
                        filterCity = province
                        filterAge = age
                        if (TextUtils.isEmpty(filterCity)){
                            setLocationTitleValue("不限")
                        }else{
                            setLocationTitleValue(filterCity)
                        }
                        refreshUserList()
                    }
                })
        }else{
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
            if (LocationCacheManager.mapLocation != null) LocationCacheManager.mapLocation!!.province else "不限",
            "动态"
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

    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initVpData() {
        frgList.add(UserListFrg())

        frgList.add(DynamicListFrg())

        mBinding.viewPager.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    mBinding.tvFilter.visibility = View.VISIBLE
                    mBinding.vgDynamicCount.visibility = View.INVISIBLE
                } else {
                    mBinding.tvFilter.visibility = View.INVISIBLE
                    mBinding.vgDynamicCount.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        LiveEventBus.get<Int>(LiveDataEventManager.SWITCH_TO_SAME_CITY).observe(this) {
            getLocation()
        }
    }

    private fun getLocation() {
        if (!isGetLocation) {
            isGetLocation = true
            LocationUtils.getTencentLocation(
                mContext,
                object : LocationUtils.OnLocationResultListener {
                    override fun onLocationResult(aMapLocation: TencentLocation?) {
                        if (aMapLocation != null) {
                            setLocationTitleValue(aMapLocation.province)
                            filterCity = aMapLocation.province
                        }
                        refreshUserList()
                    }

                })
        }
    }

    private fun setLocationTitleValue(province:String){
        val pagerTitleView =
            commonNavigator.getPagerTitleView(0) as CommonIndicatorTitleView
        pagerTitleView.text = province
    }
}
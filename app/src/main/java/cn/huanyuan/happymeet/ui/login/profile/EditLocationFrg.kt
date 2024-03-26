package cn.huanyuan.happymeet.ui.login.profile

import android.annotation.SuppressLint
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgEditLocationBinding
import cn.huanyuan.happymeet.ui.login.LoginViewModel
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.pcl.sdklib.sdk.location.LocationUtils
import com.tencent.map.geolocation.TencentLocation

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
class EditLocationFrg : BaseFragment<FrgEditLocationBinding, LoginViewModel>(
    R.layout.frg_edit_location,
    LoginViewModel::class.java
) {
    override fun initData() {
        val selfViewModel = (context as CompleteProfileActivity).mViewModel
        mBinding.tvProvince.setOnSingleClickListener {
            LocationUtils.getTencentLocation(mContext,object : LocationUtils.OnLocationResultListener{
                @SuppressLint("SetTextI18n")
                override fun onLocationResult(aMapLocation: TencentLocation?) {
                    if (aMapLocation!=null){
                        val value = selfViewModel.personInfo.value
                        value?.province = aMapLocation.province
                        value?.city = aMapLocation.city
                        mBinding.tvProvince.text = "${aMapLocation.province}·${aMapLocation.city}"
                    }
                }

            })
        }
    }


    companion object {
        fun newsInstance(): EditLocationFrg {
            return EditLocationFrg()
        }
    }
}
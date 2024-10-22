package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopRoseRechargeBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.huanyuan.sweetlove.ui.wallet.adapter.RoseRechargeAdapter
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.RoseRechargeBean
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager

/**
 * @author: zhengjun
 * created: 2024/3/26
 * desc:玫瑰充值弹框
 */
@SuppressLint("ViewConstructor")
class RoseRechargePop(val mContext: FragmentActivity, val isDismissWhenPaySuccess: Boolean) :
    BottomPopupView(mContext) {
    override fun getImplLayoutId(): Int {
        return cn.huanyuan.sweetlove.R.layout.pop_rose_recharge
    }

    private lateinit var mBinding: PopRoseRechargeBinding
    private val roseRechargeAdapter by lazy { RoseRechargeAdapter() }
    private var selectItem: RoseRechargeBean? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRoseRechargeBinding.bind(popupImplView)
        mBinding?.apply {
            initRechargeAdapter()
            ivRecharge.setOnSingleClickListener {
                PayManager.toPay(mContext, paySelectView.getSelectType(), selectItem!!.id)
            }
            getRechargeInfo()
        }
        if (!TextUtils.isEmpty(AppCacheManager.rechargeInfo)){
            val rechargeResponse = GsonUtils.fromJson(AppCacheManager.rechargeInfo,RoseRechargeResponse::class.java)
            setRechargeInfo(rechargeResponse, false)
        }
        onRechargeResultListener()
        setAgreementInfo()
    }

    private fun onRechargeResultListener() {
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                if (isDismissWhenPaySuccess) {
                    dismiss()
                    return
                }
                getRechargeInfo(true)
            }
        })
    }

    private var rechargeAgreements:String = ""
    private fun setAgreementInfo() {
        val build = Spans.builder().text("充值即代表同意")
            .text("${CommonUtils.getString(R.string.app_name)}充值协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue)).click(
                mBinding.tvRechargeAgreement,
                CustomClickSpan(mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue), object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        RouteIntent.lunchToWebView(WebUrlManager.RECHARGE_AGREEMENT)
                    }
                })
            ).build()
        mBinding.tvRechargeAgreement.text = build
    }

    private fun getRechargeInfo(isRefreshBalance: Boolean = false) {
        request({ rxApi.getRechargeInfo() },
            object : OnRequestResultListener<RoseRechargeResponse> {
                override fun onSuccess(data: BaseBean<RoseRechargeResponse>) {
                    val rechargeResponse = data.data
                    setRechargeInfo(rechargeResponse, isRefreshBalance)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                }
            })
    }

    private fun setRechargeInfo(
        rechargeResponse: RoseRechargeResponse?,
        isRefreshBalance: Boolean
    ) {
        rechargeResponse?.apply {
            rechargeAgreements = this.rechargeAgreement
            mBinding.tvRoseBalance.text = roseBalance
            if (!isRefreshBalance) {
                selectItem = list[defaultSelect]
                mBinding.selectItem = selectItem
                roseRechargeAdapter.submitList(list)
            }
        }
    }


    private fun PopRoseRechargeBinding.initRechargeAdapter() {
        rvRose.adapter = roseRechargeAdapter
        roseRechargeAdapter.setOnItemClickListener { _, _, position ->
            selectItem = roseRechargeAdapter.getItem(position)
            mBinding?.selectItem = selectItem
            roseRechargeAdapter.setSelectPosition(
                position
            )
        }
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            hasShadowBg: Boolean = true,
            isDismissWhenPaySuccess: Boolean = false
        ): RoseRechargePop {
            val roseRechargePop = RoseRechargePop(mContext, isDismissWhenPaySuccess)
            val builder = XPopup.Builder(mContext)
            builder
                .hasShadowBg(hasShadowBg)
                .asCustom(roseRechargePop).show()
            return roseRechargePop!!
        }
    }
}
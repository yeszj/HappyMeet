package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.BuyLiveTimeAdapter
import cn.yanhu.agora.bean.LiveTimePriceInfo
import cn.yanhu.agora.databinding.PopBuyLiveTimeBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.router.RouteIntent
import com.blankj.utilcode.util.AppUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager

/**
 * @author: zhengjun
 * created: 2024/3/21
 * desc:
 */
@SuppressLint("ViewConstructor")
class BuyLiveTimePop(
    val mContext: FragmentActivity,
    val list: MutableList<LiveTimePriceInfo>,
    val rechargeAgreement: String
) :
    BottomPopupView(mContext) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_buy_live_time
    }

    private val timePriceAdapter by lazy { BuyLiveTimeAdapter() }
    private var mBinding: PopBuyLiveTimeBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopBuyLiveTimeBinding.bind(popupImplView)
        mBinding?.apply {
            initPriceAdapter()
            setAgreementInfo()
            ivClose.setOnSingleClickListener { dismiss() }
            ivRecharge.setOnSingleClickListener {
                val selectItem = timePriceAdapter.getSelectItem()
                selectItem?.apply {
                    PayManager.toPay(
                        mContext,
                        paySelectView.getSelectType(),
                        selectItem.id,
                        PayManager.RECHARGE_TYPE_LIVE_TIME
                    )
                }
            }
        }
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                dismiss()
            }
        })
    }


    private fun setAgreementInfo() {
        val build = Spans.builder().text("充值即代表同意")
            .text("${AppUtils.getAppName()}充值协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue)).click(
                mBinding?.tvRechargeAgreement,
                CustomClickSpan(mContext, object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        RouteIntent.lunchToWebView(rechargeAgreement)
                    }
                })
            ).build()
        mBinding?.tvRechargeAgreement?.text = build
    }


    private fun PopBuyLiveTimeBinding.initPriceAdapter() {
        recyclerView.adapter = timePriceAdapter
        timePriceAdapter.submitList(list)
        setSelectPrice()
        timePriceAdapter.setOnItemClickListener { _, _, position ->
            timePriceAdapter.setSelectPosition(position)
            setSelectPrice()
        }
    }

    private fun setSelectPrice() {
        val item = timePriceAdapter.getSelectItem()
        mBinding?.tvAmount?.text = item?.price
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity, list: MutableList<LiveTimePriceInfo>,  rechargeAgreement:String
        ): BuyLiveTimePop {
            val matchPop = BuyLiveTimePop(mContext, list,rechargeAgreement)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
package cn.huanyuan.happymeet.func.dialog

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.databinding.PopRoseRechargeBinding
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.huanyuan.happymeet.ui.wallet.adapter.RoseRechargeAdapter
import cn.yanhu.commonres.bean.RoseRechargeBean
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
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
        return cn.huanyuan.happymeet.R.layout.pop_rose_recharge
    }

    private var mBinding: PopRoseRechargeBinding? = null
    private val roseRechargeAdapter by lazy { RoseRechargeAdapter() }
    private var selectItem: RoseRechargeBean? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRoseRechargeBinding.bind(popupImplView)
        mBinding?.apply {
            initRechargeAdapter()
            btnPay.setOnSingleClickListener {
                PayManager.toPay(mContext, paySelectView.getSelectType(), selectItem!!.id)
            }
            getRechargeInfo()
        }
        onRechargeResultListener()
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


    private fun getRechargeInfo(isRefreshBalance: Boolean = false) {
        request({ rxApi.getRechargeInfo() },
            object : OnRequestResultListener<RoseRechargeResponse> {
                override fun onSuccess(data: BaseBean<RoseRechargeResponse>) {
                    val rechargeResponse = data.data
                    rechargeResponse?.apply {
                        mBinding?.tvRoseBalance?.text = roseBalance
                        if (!isRefreshBalance) {
                            selectItem = list[0]
                            roseRechargeAdapter.submitList(list)
                        }
                    }
                    hideLoading()
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    hideLoading()
                }
            })
    }

    private fun hideLoading() {
        mBinding?.vgLoading?.visibility = GONE
        this@RoseRechargePop.doShowAnimation()
    }

    private fun PopRoseRechargeBinding.initRechargeAdapter() {
        rvRose.adapter = roseRechargeAdapter
        roseRechargeAdapter.setOnItemClickListener { _, _, position ->
            selectItem = roseRechargeAdapter.getItem(position)
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
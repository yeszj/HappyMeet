package cn.huanyuan.sweetlove.ui.wallet

import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityRoseRechargeBinding
import cn.huanyuan.sweetlove.ui.wallet.adapter.RoseRechargeAdapter
import cn.huanyuan.sweetlove.ui.wallet.detail.WalletDetailActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.RoseRechargeBean
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager

/**
 * @author: zhengjun
 * created: 2024/3/7
 * desc:玫瑰充值页面
 */
@Route(path = RouterPath.ROUTER_ROSE_RECHARGE)
class RoseRechargeActivity : BaseActivity<ActivityRoseRechargeBinding, WalletViewModel>(
    R.layout.activity_rose_recharge,
    WalletViewModel::class.java
) {
    private val roseRechargeAdapter by lazy { RoseRechargeAdapter() }
    private var selectItem: RoseRechargeBean? = null
    override fun initData() {
        setFullScreenStatusBar(false)
        initRechargeAdapter()
        setAgreementInfo()
        requestData()
    }

    private fun setAgreementInfo() {
        val build = Spans.builder().text("充值即代表同意")
            .text("${CommonUtils.getString(R.string.app_name)}充值协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.adminTagColor)).click(
                mBinding.tvRechargeAgreement,
                CustomClickSpan(mContext,CommonUtils.getColor(cn.yanhu.baselib.R.color.adminTagColor), object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        RouteIntent.lunchToWebView(WebUrlManager.RECHARGE_AGREEMENT)
                    }
                })
            ).build()
        mBinding.tvRechargeAgreement.text = build
    }

    override fun initRefresh() {
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refreshLayout)
                }
            })
    }

    override fun initListener() {
        mBinding.ivRecharge.setOnSingleClickListener {
            PayManager.toPay(mContext, mBinding.paySelectView.getSelectType(), selectItem!!.id)
        }
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                WalletDetailActivity.lunch(mContext, WalletDetailActivity.TYPE_ROSE_DETAIL)
            }
        })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRechargeInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onGetRechargeInfoResult()
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                requestData()
            }
        })
    }

    private var rechargeAgreement: String = ""
    private fun onGetRechargeInfoResult() {
        mViewModel.rechargeInfoLivedata.observe(this) { it ->
            parseState(it, {
                rechargeAgreement = it.rechargeAgreement
                val list = it.list
                selectItem = list[0]
                mBinding.selectItem = selectItem
                mBinding.tvRoseNum.text = it.roseBalance
                roseRechargeAdapter.submitList(list)
            })
        }
    }

    private fun initRechargeAdapter() {
        mBinding.rvRose.adapter = roseRechargeAdapter
        roseRechargeAdapter.setOnItemClickListener { _, _, position ->
            selectItem = roseRechargeAdapter.getItem(position)
            mBinding.selectItem = selectItem
            roseRechargeAdapter.setSelectPosition(
                position
            )
        }
    }
}
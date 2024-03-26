package cn.huanyuan.happymeet.ui.wallet

import android.content.Context
import android.content.Intent
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityRoseExchangeBinding
import cn.huanyuan.happymeet.ui.wallet.adapter.RoseExchangePrivilegeAdapter
import cn.huanyuan.happymeet.ui.wallet.adapter.RoseRechargeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.RoseRechargeBean
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class RoseExchangeActivity : BaseActivity<ActivityRoseExchangeBinding, WalletViewModel>(
    R.layout.activity_rose_exchange,
    WalletViewModel::class.java
) {
    private val roseRechargeAdapter by lazy { RoseRechargeAdapter() }
    private val privilegeAdapter by lazy { RoseExchangePrivilegeAdapter() }
    private var selectItem: RoseRechargeBean? = null
    override fun initData() {
        setFullScreenStatusBar(true)
        initRechargeAdapter()
        mBinding.rvPrivilege.adapter = privilegeAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRoseExchangeInfo()
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnExchange.setOnSingleClickListener {
            selectItem?.apply {
                if (CommonUtils.compareString(balance, this.money)) {
                    DialogUtils.showConfirmDialog("确定要兑换玫瑰${this.roseNum}支吗？",{
                        mViewModel.roseExchange(selectItem!!.id)
                    },{
                    }, confirm = "确定")
                } else {
                    showToast("钱包余额不足")
                }
            }
        }
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

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onGetExchangeInfoResult()
        mViewModel.exchangeResultLivedata.observe(this){ it ->
            parseState(it,{
                showToast("兑换成功")
                balance = it
                setBalance()
            })
        }
    }

    private fun onGetExchangeInfoResult() {
        mViewModel.exchangeInfoLivedata.observe(this) { it ->
            parseState(it, {
                val list = it.list
                selectItem = list[0]
                mBinding.selectItem = selectItem
                balance = it.balance
                setBalance()
                privilegeAdapter.submitList(it.privileges)
                roseRechargeAdapter.submitList(list)
            })
        }
    }

    private var balance: String = "0"
    private fun setBalance() {
        mBinding.tvWalletBalance.text = balance
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

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, RoseExchangeActivity::class.java))
        }
    }

}
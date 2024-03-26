package cn.huanyuan.happymeet.ui.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityWithdrawalBinding
import cn.huanyuan.happymeet.ui.userinfo.auth.AliPayAccountBindActivity
import cn.huanyuan.happymeet.ui.userinfo.auth.RealNameActivity
import cn.huanyuan.happymeet.ui.wallet.adapter.WithdrawalAdapter
import cn.huanyuan.happymeet.ui.wallet.detail.WalletDetailActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.bean.WithDrawInfo
import cn.yanhu.commonres.bean.response.WithdrawResponse
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.view.PayWaySelectView
import cn.zj.netrequest.ext.parseState
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.sdk.wechat.WxAuthUtils

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class WithdrawalActivity : BaseActivity<ActivityWithdrawalBinding, WalletViewModel>(
    R.layout.activity_withdrawal,
    WalletViewModel::class.java
) {
    private val withdrawalAdapter by lazy { WithdrawalAdapter() }
    private var selectItem: WithDrawInfo? = null
    private var selectAccountType:Int = PayWayInfo.TYPE_ALIPAY
    override fun initData() {
        setFullScreenStatusBar(true)
        mBinding.rvAmount.adapter = withdrawalAdapter
        withdrawalAdapter.setOnItemClickListener { _, _, position ->
            selectItem = withdrawalAdapter.getItem(position)
            withdrawalAdapter.setSelectPosition(position)
        }
        setAgreementInfo()
        requestData()
    }

    private fun setAgreementInfo() {
        val build = Spans.builder().text("我已阅读并同意")
            .text("用户提现协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue)).click(
                mBinding.tvAgreement,
                CustomClickSpan(mContext, object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        RouteIntent.lunchToWebView(withDrawInfo!!.withdrawalAgreement)
                    }
                })
            ).build()
        mBinding.tvAgreement.text = build
    }

    private var isAgree: Boolean = false
    override fun initListener() {
        mBinding.ivCheck.setOnClickListener {
            clickAgreement()
        }
        mBinding.tvWithdrawal.setOnSingleClickListener {
            startWithdrawal()
        }
        initTitleListener()
        mBinding.paySelectView.registerSelectChangeListener(object : PayWaySelectView.OnSelectChangeListener{
            override fun onSelectChange(type: Int) {
                selectAccountType = type
                bindAccountInfo()
            }

        })
    }

    private fun clickAgreement() {
        isAgree = !isAgree
        if (isAgree) {
            mBinding.ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_selected_r20)
        } else {
            mBinding.ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_unselected_r20)
        }
    }

    private fun startWithdrawal() {
        withDrawInfo?.apply {
            if (isAgree) {

                //已经实名认证
                if (!TextUtils.isEmpty(withDrawInfo?.realName)) {
                    val selectType = mBinding.paySelectView.getSelectType()
                    if (mBinding.paySelectView.getSelectType() == PayWayInfo.TYPE_ALIPAY) {
                        if (TextUtils.isEmpty(withDrawInfo?.aliAccount)) {
                            showToast("请先绑定支付宝账户")
                            toAliAccountBind()
                            return@apply
                        }
                    } else {
                        if (TextUtils.isEmpty(withDrawInfo?.wxNickName)) {
                            showToast("请先绑定微信账户")
                            toWxAuth()
                            return@apply
                        }
                    }
                    mViewModel.withdrawal(selectType, selectItem!!.id)
                } else {
                    //实名认证
                    toRealName()
                }
            } else {
                showToast("请同意并勾选用户提现协议")
            }
        }

    }

    private fun toAliAccountBind() {
        AliPayAccountBindActivity.lunch(mContext, withDrawInfo!!.realName)
    }


    private fun initTitleListener() {
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                WalletDetailActivity.lunch(mContext, WalletDetailActivity.TYPE_COIN_DETAIL)
            }
        })
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

    override fun requestData() {
        super.requestData()
        mViewModel.getWithdrawalInfo()
        WxAuthUtils.registerAuthResultListener(mContext, object : OnAuthResultListener {
            override fun onAuthSuccess() {
                showToast("已绑定")
                requestData()
            }
        })
    }

    private var withDrawInfo: WithdrawResponse? = null
    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.withdrawalResultLivedata.observe(this) {
            parseState(it, {
                showToast("提现成功")
                requestData()
            })
        }
        mViewModel.withdrawalInfoLivedata.observe(this) { it ->
            parseState(it, {
                withDrawInfo = it

                bindAccountInfo()

                mBinding.tvBalance.text = it.balance
                val list = it.list
                selectItem = list[0]
                mBinding.tvRule.text = it.withdrawalRule
                withdrawalAdapter.submitList(list)
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindAccountInfo() {
        withDrawInfo?.apply {

            if (selectAccountType == PayWayInfo.TYPE_ALIPAY) {
                val aliAccount = if (TextUtils.isEmpty(this.aliAccount)) {
                    mBinding.tvBind.text = "立即绑定"
                    "未绑定"
                } else {
                    mBinding.tvBind.text = "更换绑定"
                    this.aliAccount
                }
                mBinding.tvAccount.text =  "支付宝账号        $aliAccount"
                mBinding.ivWxAvatar.visibility = View.GONE
                mBinding.tvBind.setOnSingleClickListener { toAliAccountBind() }
            } else {
                val wxNickName = if (TextUtils.isEmpty(this.wxNickName)) {
                    mBinding.ivWxAvatar.visibility = View.GONE
                    mBinding.tvBind.text = "立即绑定"
                    "未绑定"
                } else {
                    mBinding.ivWxAvatar.visibility = View.VISIBLE
                    mBinding.tvBind.text = "更换绑定"
                    GlideUtils.load(mContext,this.wxAvatar,mBinding.ivWxAvatar)
                    this.wxNickName
                }
                mBinding.tvAccount.text =  "微信昵称           $wxNickName"
                mBinding.tvBind.setOnSingleClickListener { toWxAuth() }
            }
            val name = if (TextUtils.isEmpty(this.realName)) {
                mBinding.tvBind.text = "立即认证"
                mBinding.tvBind.setOnSingleClickListener { toRealName() }
                "未认证"
            } else {
                this.realName
            }
            mBinding.tvRealName.text = "真实姓名           $name"
        }
    }

    private fun toWxAuth() {
        WxAuthUtils.weChatAuth(mContext)
    }

    private fun toRealName() {
        RealNameActivity.lunch(mContext)
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, WithdrawalActivity::class.java))
        }
    }
}
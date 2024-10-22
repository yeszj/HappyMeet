package cn.huanyuan.sweetlove.ui.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityWithdrawalBinding
import cn.huanyuan.sweetlove.ui.userinfo.auth.RealNameActivity
import cn.huanyuan.sweetlove.ui.wallet.adapter.WithdrawalAdapter
import cn.huanyuan.sweetlove.ui.wallet.detail.WalletDetailActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.bean.WithDrawInfo
import cn.yanhu.commonres.bean.response.WithdrawResponse
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.view.PayWaySelectView
import cn.zj.netrequest.ext.parseState
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.sdk.alipay.AliAuthUtils
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
        setFullScreenStatusBar(false)
        mBinding.rvAmount.adapter = withdrawalAdapter
        withdrawalAdapter.setOnItemClickListener { _, _, position ->
            selectItem = withdrawalAdapter.getItem(position)
            setWithdrawRule()
            withdrawalAdapter.setSelectPosition(position)
        }
        setAgreementInfo()
        requestData()
    }

    private fun setAgreementInfo() {
        val build = Spans.builder().text("我已阅读并同意")
            .text("用户提现协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.adminTagColor)).click(
                mBinding.tvAgreement,
                CustomClickSpan(mContext,CommonUtils.getColor(cn.yanhu.baselib.R.color.adminTagColor), object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        RouteIntent.lunchToWebView(WebUrlManager.WITHDRAW_AGREEMENT)
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
                            toAliAuth()
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

    private fun toAliAuth() {
        AliAuthUtils.aliAuth(mContext,object : OnAuthResultListener{
            override fun onAuthSuccess() {
                requestData()
            }
        })
       // AliPayAccountBindActivity.lunch(mContext, withDrawInfo!!.realName)
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
                showToast("提现申请提交成功，请耐心等待")
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
                setWithdrawRule()
                withdrawalAdapter.submitList(list)
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setWithdrawRule(){
        mBinding.tvRule.text = "1.提现时间：9:00-18:00，每日可提现5次。\n" +
                "2.本平台支持提现10分钟内到账，申请提现后请及时查看到账情况，超过24小时未到账请联系客服。\n" +
                "3.提现时将从提现金额中扣除${CommonUtils.multiplyString(selectItem!!.ratio.toPlainString(),"100")}%作为手续费和平台服务费。"
    }

    @SuppressLint("SetTextI18n")
    private fun bindAccountInfo() {
        withDrawInfo?.apply {
            if (selectAccountType == PayWayInfo.TYPE_ALIPAY) {
                bindAliStyle()
            } else {
                bindWxStyle()
            }
            val name = if (TextUtils.isEmpty(this.realName)) {
                "未认证"
            } else {
                this.realName
            }
            mBinding.tvRealName.text = name
        }
    }

    private fun WithdrawResponse.bindWxStyle() {
        mBinding.tvBind.setTextColor(Color.parseColor("#00CB76"))
        if (TextUtils.isEmpty(this.wxNickName)) {
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35))
            mBinding.vgNoBind.visibility = View.VISIBLE
            mBinding.vgNoBind.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_wx_no_bind)
            mBinding.tvClickBind.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#00CB76"))
            mBinding.tvNobindTip.text = "您还未绑定微信账户"
            mBinding.bgAccountInfo.visibility = View.INVISIBLE
            mBinding.tvClickBind.setOnSingleClickListener {
                if (TextUtils.isEmpty(this.realName)) {
                    showToast("请先进行实名认证")
                    toRealName()
                } else {
                    toWxAuth()
                }
            }
        } else {
            mBinding.tvAccountTag.text = "微信账号"
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            mBinding.bgAccountInfo.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_wx_no_bind)
            mBinding.bgAccountInfo.visibility = View.VISIBLE
            mBinding.vgNoBind.visibility = View.GONE
            mBinding.tvAccount.text = wxNickName
            mBinding.tvBind.setOnSingleClickListener { toWxAuth() }
        }
    }

    private fun WithdrawResponse.bindAliStyle() {
        mBinding.tvBind.setTextColor(Color.parseColor("#2D9AFF"))
        if (TextUtils.isEmpty(this.aliAccount)) {
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35))
            mBinding.vgNoBind.visibility = View.VISIBLE
            mBinding.vgNoBind.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_ali_no_bind)
            mBinding.tvClickBind.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#2D9AFF"))
            mBinding.tvNobindTip.text = "您还未绑定支付宝账户"
            mBinding.bgAccountInfo.visibility = View.INVISIBLE
            mBinding.tvClickBind.setOnSingleClickListener {
                if (TextUtils.isEmpty(this.realName)) {
                    showToast("请先进行实名认证")
                    toRealName()
                } else {
                    toAliAuth()
                }
            }
        } else {
            mBinding.tvAccountTag.text = "支付宝账号"
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            mBinding.bgAccountInfo.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_ali_no_bind)
            mBinding.bgAccountInfo.visibility = View.VISIBLE
            mBinding.vgNoBind.visibility = View.GONE
            mBinding.tvAccount.text = aliAccount
            mBinding.tvBind.setOnSingleClickListener { toAliAuth() }
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
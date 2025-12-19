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
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.CommonErrorTipsInfo
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.bean.WithDrawInfo
import cn.yanhu.commonres.bean.response.WithdrawResponse
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.view.PayWaySelectView
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.GsonUtils
import com.lxj.xpopup.core.BasePopupView
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.sdk.alipay.AliAuthUtils
import com.pcl.sdklib.sdk.wechat.WxAuthUtils
import androidx.core.graphics.toColorInt
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.login.SetPwdActivity
import cn.huanyuan.sweetlove.ui.wallet.bank.BindBankActivity
import cn.yanhu.commonres.adapter.MyBannerImageAdapter
import cn.yanhu.commonres.bean.AuthCenterInfo
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.youth.banner.listener.OnBannerListener

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
        getAuthCenterInfo()
        setAgreementInfo()
        requestData()
    }

    private var authCenterInfo: AuthCenterInfo? = null
    fun getAuthCenterInfo() {
        request({ rxApi.getAuthCenterInfo() }, object : OnRequestResultListener<AuthCenterInfo>{
            override fun onSuccess(data: BaseBean<AuthCenterInfo>) {
                authCenterInfo = data.data
            }

        })
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
        LiveEventBus.get<Boolean>(EventBusKeyConfig.BINDBANKSUCCESS).observe(this) {
            requestData()
        }
        WxAuthUtils.registerAuthResultListener(mContext, object : OnAuthResultListener {
            override fun onAuthSuccess() {
                showToast("已绑定")
                requestData()
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
                    } else if (mBinding.paySelectView.getSelectType() == PayWayInfo.TYPE_WXPAY){
                        if (TextUtils.isEmpty(withDrawInfo?.wxNickName)) {
                            showToast("请先绑定微信账户")
                            toWxAuth()
                            return@apply
                        }
                    }else{
                        if (TextUtils.isEmpty(withDrawInfo?.bankCard)) {
                            showToast("请先绑定银行卡")
                            toBindBankCard()
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

    }

    private var withDrawInfo: WithdrawResponse? = null
    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.withdrawalResultLivedata.observe(this) { it ->
            parseState(it, {
                showToast("提现申请提交成功，请耐心等待")
                requestData()
            },{
                if (it.code==ErrorCode.COMMON_TIP_POP){
                    showErrorTip(it)
                }else if (it.code == ErrorCode.CODE_SET_PWD){
                    showToast(it.msg)
                    SetPwdActivity.lunch(mContext,authCenterInfo?.phone.toString())
                } else{
                    showToast(it.msg)
                }
            })
        }
        mViewModel.withdrawalInfoLivedata.observe(this) { it ->
            parseState(it, {
                withDrawInfo = it
                it.withdrawTypeList?.apply {
                    mBinding.paySelectView.setPayList(this)
                }
                bindAccountInfo()
                bindBanner(it.banners)
                mBinding.tvBalance.text = it.balance
                val list = it.list
                selectItem = list[0]
                setWithdrawRule()
                withdrawalAdapter.submitList(list)
            })
        }
    }

    private var bannerImageAdapter: MyBannerImageAdapter?=null
    private fun bindBanner(list: MutableList<BannerBean>) {
        if (list.isEmpty()){
            mBinding.banner.visibility = View.GONE
        }else{
            mBinding.banner.visibility = View.VISIBLE
            if (bannerImageAdapter==null){
                mBinding.banner.addBannerLifecycleObserver(this)
                bannerImageAdapter = MyBannerImageAdapter(mBinding.banner, list)
                mBinding.banner.setAdapter(bannerImageAdapter)
                mBinding.banner.setOnBannerListener(object : OnBannerListener<BannerBean> {
                    override fun OnBannerClick(data: BannerBean, position: Int) {
                        PageIntentUtil.url2Page(ActivityUtils.getTopActivity(), data.pageUrl)
                    }
                })
            }else{
                bannerImageAdapter?.setDatas(list)
            }
        }
    }

    private var errorTipsPop:BasePopupView?=null
    private fun showErrorTip(it: CustomException) {
        if (CommonUtils.isPopShow(errorTipsPop)){
            return
        }
        val msg = it.msg
        val commonTipsInfo = GsonUtils.fromJson(
            msg,
            CommonErrorTipsInfo::class.java
        )
        errorTipsPop = DialogUtils.showConfirmDialog(commonTipsInfo.title, {
            PageIntentUtil.url2Page(mContext, commonTipsInfo.pageUrl)
        }, {
        }, commonTipsInfo.content, commonTipsInfo.leftBtn, commonTipsInfo.rightBtn)
    }

    @SuppressLint("SetTextI18n")
    private fun setWithdrawRule(){
        mBinding.tvRule.text = withDrawInfo?.desc
    }

    @SuppressLint("SetTextI18n")
    private fun bindAccountInfo() {
        withDrawInfo?.apply {
            if (selectAccountType == PayWayInfo.TYPE_ALIPAY) {
                bindAliStyle()
            } else if (selectAccountType == PayWayInfo.TYPE_WXPAY) {
                bindWxStyle()
            }else{
                bindBankStyle()
            }
            val name = if (TextUtils.isEmpty(this.realName)) {
                "未认证"
            } else {
                this.realName
            }
            mBinding.tvRealName.text = name
        }
    }

    private fun WithdrawResponse.bindBankStyle() {
        mBinding.tvBind.setTextColor("#FFB039".toColorInt())
        if (TextUtils.isEmpty(this.bankCard)) {
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35))
            mBinding.vgNoBind.visibility = View.VISIBLE
            mBinding.vgNoBind.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_bank_no_bind)
            mBinding.tvClickBind.backgroundTintList =
                ColorStateList.valueOf("#FFB039".toColorInt())
            mBinding.tvNobindTip.text = "您还未绑定银行卡"
            mBinding.bgAccountInfo.visibility = View.INVISIBLE
            mBinding.tvClickBind.setOnSingleClickListener {
                if (TextUtils.isEmpty(this.realName)) {
                    showToast("请先进行实名认证")
                    toRealName()
                } else {
                    showBindTipPop()
                }
            }
        } else {
            mBinding.tvAccountTag.text = "银行卡号"
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            mBinding.bgAccountInfo.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_bank_no_bind)
            mBinding.bgAccountInfo.visibility = View.VISIBLE
            mBinding.vgNoBind.visibility = View.GONE
            mBinding.tvAccount.text = bankCard
            mBinding.tvBind.setOnSingleClickListener { showBindTipPop() }
        }
    }




    private fun showBindTipPop(){
         DialogUtils.showConfirmDialog(
            "温馨提示",{
                 if (selectAccountType == PayWayInfo.TYPE_ALIPAY) {
                     toAliAuth()
                 } else if (selectAccountType == PayWayInfo.TYPE_WXPAY) {
                     toWxAuth()
                 }else{
                     toBindBankCard()
                 }
             },{

             },
            "您好！请确认绑定的提现账号与平台的实名认证一致避免提现失败",
            "取消",
            "去绑定"
        )
    }


    private fun toBindBankCard(){
        BindBankActivity.lunch(mContext, withDrawInfo!!.realName)
    }

    private fun WithdrawResponse.bindWxStyle() {
        mBinding.tvBind.setTextColor("#00CB76".toColorInt())
        if (TextUtils.isEmpty(this.wxNickName)) {
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35))
            mBinding.vgNoBind.visibility = View.VISIBLE
            mBinding.vgNoBind.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_wx_no_bind)
            mBinding.tvClickBind.backgroundTintList =
                ColorStateList.valueOf("#00CB76".toColorInt())
            mBinding.tvNobindTip.text = "您还未绑定微信账户"
            mBinding.bgAccountInfo.visibility = View.INVISIBLE
            mBinding.tvClickBind.setOnSingleClickListener {
                if (TextUtils.isEmpty(this.realName)) {
                    showToast("请先进行实名认证")
                    toRealName()
                } else {
                    showBindTipPop()
                }
            }
        } else {
            mBinding.tvAccountTag.text = "微信账号"
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            mBinding.bgAccountInfo.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_wx_no_bind)
            mBinding.bgAccountInfo.visibility = View.VISIBLE
            mBinding.vgNoBind.visibility = View.GONE
            mBinding.tvAccount.text = wxNickName
            mBinding.tvBind.setOnSingleClickListener { showBindTipPop() }
        }
    }

    private fun WithdrawResponse.bindAliStyle() {
        mBinding.tvBind.setTextColor("#2D9AFF".toColorInt())
        if (TextUtils.isEmpty(this.aliAccount)) {
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35))
            mBinding.vgNoBind.visibility = View.VISIBLE
            mBinding.vgNoBind.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_ali_no_bind)
            mBinding.tvClickBind.backgroundTintList =
                ColorStateList.valueOf("#2D9AFF".toColorInt())
            mBinding.tvNobindTip.text = "您还未绑定支付宝账户"
            mBinding.bgAccountInfo.visibility = View.INVISIBLE
            mBinding.tvClickBind.setOnSingleClickListener {
                if (TextUtils.isEmpty(this.realName)) {
                    showToast("请先进行实名认证")
                    toRealName()
                } else {
                    showBindTipPop()
                }
            }
        } else {
            mBinding.tvAccountTag.text = "授权账号"
            ViewUtils.setPaddingTop(mBinding.tvDesc,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            mBinding.bgAccountInfo.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_ali_no_bind)
            mBinding.bgAccountInfo.visibility = View.VISIBLE
            mBinding.vgNoBind.visibility = View.GONE
            mBinding.tvAccount.text = aliAccount
            mBinding.tvBind.setOnSingleClickListener { showBindTipPop() }
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
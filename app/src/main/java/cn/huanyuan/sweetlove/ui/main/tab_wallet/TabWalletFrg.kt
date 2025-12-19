package cn.huanyuan.sweetlove.ui.main.tab_wallet

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.WalletInfo
import cn.huanyuan.sweetlove.databinding.FrgTabWalletBinding
import cn.huanyuan.sweetlove.ui.invite.BindInviteCodeActivity
import cn.huanyuan.sweetlove.ui.invite.InviteMainActivity
import cn.huanyuan.sweetlove.ui.invite.InviteMeUserActivity
import cn.huanyuan.sweetlove.ui.invite.MyInviteRecordActivity
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.wallet.RoseExchangeActivity
import cn.huanyuan.sweetlove.ui.wallet.WithdrawalActivity
import cn.huanyuan.sweetlove.ui.wallet.detail.WalletDetailActivity
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.adapter.CommonTxtBannerAdapter
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.ClipboardUtils

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class TabWalletFrg : BaseFragment<FrgTabWalletBinding, MainViewModel>(
    R.layout.frg_tab_wallet,
    MainViewModel::class.java
) {
    private var walletInfo: WalletInfo? = null
    override fun initData() {
        val toFloat = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_18).toFloat()
        mBinding.tvTitle.textSize = toFloat
        bindRewardBanner()
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getWalletInfo()
    }

    private val rewardBannerAdapter by lazy { CommonTxtBannerAdapter(mContext, mutableListOf()) }
    private fun bindRewardBanner() {
        mBinding.rewardBanner.setAdapter(rewardBannerAdapter)
        mBinding.rewardBanner.addBannerLifecycleObserver(context as FragmentActivity)
        rewardBannerAdapter.setOnBannerListener { _, _ ->
            MyInviteRecordActivity.lunch(
                mContext
            )
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.walletObservable.observe(this) { it ->
            parseState(it, {
                walletInfo = it
                rewardBannerAdapter.setDatas(it.carouselList)
                mBinding.walletInfo = it
                bindConfirmNumTips(it)
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindConfirmNumTips(data: WalletInfo) {
        val needConfirmCnt: Int = data.needConfirmCnt
        if (needConfirmCnt > 0) {
            mBinding.tvWithDrawTips.text = "你有" + needConfirmCnt + "笔提现待确认"
            mBinding.vgWithDrawTips.visibility = View.VISIBLE
        } else {
            mBinding.vgWithDrawTips.visibility = View.GONE
        }
    }
    override fun initListener() {
        super.initListener()
        mBinding.btnWithDraw.setOnSingleClickListener { WithdrawalActivity.lunch(mContext) }
        mBinding.btnExchange.setOnSingleClickListener {
            RoseExchangeActivity.lunch(mContext)
        }
        mBinding.tvWithdrawalDetail.setOnSingleClickListener {
            PageIntentUtil.url2Page(mContext,walletInfo?.url)
        }
        mBinding.btnInvite.setOnSingleClickListener { InviteMainActivity.lunch(mContext) }
        mBinding.vgInvite.setOnSingleClickListener {
            if (TextUtils.isEmpty(walletInfo?.inviteNickName)) {
                BindInviteCodeActivity.lunch(mContext)
            } else {
                InviteMeUserActivity.lunch(mContext)
            }
        }
        mBinding.btnDetail.setOnSingleClickListener {
            MyInviteRecordActivity.lunch(mContext)
        }
        mBinding.tvCopy.setOnSingleClickListener {
            ClipboardUtils.copyText(walletInfo?.userId)
            showToast("复制成功")
        }
        mBinding.tvDetail.setOnSingleClickListener {
            WalletDetailActivity.lunch(mContext, WalletDetailActivity.TYPE_COIN_DETAIL)
        }
        mBinding.tvRank.setOnSingleClickListener {
            MyInviteRecordActivity.lunch(mContext)
        }
    }

    override fun initRefresh() {
        RefreshManager.getInstance()
            .initRefresh(context, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refreshLayout)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        requestData()
    }
}
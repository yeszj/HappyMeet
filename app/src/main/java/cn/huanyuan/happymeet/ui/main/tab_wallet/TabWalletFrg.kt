package cn.huanyuan.happymeet.ui.main.tab_wallet

import android.text.TextUtils
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.WalletInfo
import cn.huanyuan.happymeet.databinding.FrgTabWalletBinding
import cn.huanyuan.happymeet.ui.invite.InviteMeUserActivity
import cn.huanyuan.happymeet.ui.invite.MyInviteRecordActivity
import cn.huanyuan.happymeet.ui.invite.BindInviteCodeActivity
import cn.huanyuan.happymeet.ui.invite.InviteMainActivity
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.wallet.RoseExchangeActivity
import cn.huanyuan.happymeet.ui.wallet.WithdrawalActivity
import cn.huanyuan.happymeet.ui.wallet.detail.WalletDetailActivity
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.ClipboardUtils
import com.jeremyliao.liveeventbus.LiveEventBus

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
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getWalletInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.walletObservable.observe(this) { it ->
            parseState(it, {
                walletInfo = it
                mBinding.walletInfo = it
            })
        }
        LiveEventBus.get<String>(LiveDataEventManager.BIND_CODE_SUCCESS).observe(this) {
            requestData()
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnWithDraw.setOnSingleClickListener { WithdrawalActivity.lunch(mContext) }
        mBinding.btnExchange.setOnSingleClickListener {
            RoseExchangeActivity.lunch(mContext)
        }
        mBinding.btnInvite.setOnSingleClickListener { InviteMainActivity.lunch(mContext) }
        mBinding.tvInviteMyTitle.setOnSingleClickListener {
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
}
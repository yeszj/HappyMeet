package cn.huanyuan.sweetlove.ui.userinfo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.RankInfo
import cn.huanyuan.sweetlove.databinding.ActivityGuardRankBinding
import cn.huanyuan.sweetlove.ui.userinfo.adapter.GuardRankAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.CommonEventPopInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.pop.CommonImagePop
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.imchat.manager.ImChatManager
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ResourceUtils

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:守护榜
 */
@Route(path = RouterPath.ROUTER_GUARD_RANK)
class GuardRankActivity : BaseActivity<ActivityGuardRankBinding, UserViewModel>(
    R.layout.activity_guard_rank, UserViewModel::class.java
) {
    private val guardRankAdapter by lazy { GuardRankAdapter() }
    private var userId: String = ""
    override fun initData() {
        setFullScreenStatusBar()
        userId = intent.getStringExtra(IntentKeyConfig.ID).toString()
        addEmptyView()
        mBinding.rvRank.adapter = guardRankAdapter
        if (userId==AppCacheManager.userId){
            mBinding.vgMyInfo.clUser.visibility = View.GONE
        }
        mBinding.vgMyInfo.clUser.setBackgroundColor(Color.parseColor("#0A000000"))
        requestData()
    }

    private fun addEmptyView() {
        val emptyView =
            LayoutInflater.from(mContext).inflate(R.layout.empty_guard_rank, null, false)
        val tvGuard = emptyView.findViewById<TextView>(R.id.tv_guard)
        tvGuard.setOnSingleClickListener {
            ImChatManager.toImChatPage(mContext, userId)
        }
        guardRankAdapter.stateView = emptyView
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refreshLayout)
                }
            })
    }

    override fun initListener() {
        super.initListener()
        mBinding.apply {
            ivAvatar1.setOnSingleClickListener {
                mBinding.rankInfo1?.apply {
                    intentToDetail(this)
                }
            }
            ivAvatar2.setOnSingleClickListener {
                mBinding.rankInfo2?.apply {
                    intentToDetail(this)
                }
            }
            ivAvatar3.setOnSingleClickListener {
                mBinding.rankInfo3?.apply {
                    intentToDetail(this)
                }
            }
            titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener {
                override fun leftButtonOnClick(v: View?) {
                    finish()
                }
                override fun rightButtonOnClick(v: View?) {
                    showGuardRulePop()
                }
            })
        }
        guardRankAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            intentToDetail(item!!)
        }
    }

    private var commonImagePop:CommonImagePop?=null
    private fun showGuardRulePop() {
        if (CommonUtils.isPopShow(commonImagePop)){
            return
        }
        val commonEventPopInfo = CommonEventPopInfo(
            "", "", true, CommonUtils.getDimension(
                com.zj.dimens.R.dimen.dp_320
            )
        )
        val drawable = ResourceUtils.getDrawable(R.mipmap.guard_rule)
        commonImagePop =  CommonImagePop.showDialog(mContext, commonEventPopInfo, drawable)
    }

    private fun intentToDetail(rankInfo: RankInfo) {
        if (!TextUtils.isEmpty(rankInfo.userId)) {
            RouteIntent.lunchPersonHomePage(rankInfo.userId)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getGuardRankList(userId)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.guardRankObservable.observe(this) { it ->
            parseState(it, {
                mBinding.vgMyInfo.rankInfo = it.myInfoRes
                val rankingList = it.guardUsers
                guardRankAdapter.isStateViewEnable = rankingList.size < 4
                if (rankingList.size > 0) {
                    mBinding.rankInfo1 = rankingList[0]
                    rankingList.removeAt(0)
                    if (rankingList.size > 0) {
                        mBinding.rankInfo2 = rankingList[0]
                        rankingList.removeAt(0)
                    }
                    if (rankingList.size > 0) {
                        mBinding.rankInfo3 = rankingList[0]
                        rankingList.removeAt(0)
                    }
                    guardRankAdapter.submitList(rankingList)
                } else {
                    mBinding.rankInfo1 = it.noGuardInfo
                }
            })
        }
    }

    companion object {
        fun lunch(context: Context, userId: String) {
            val intent = Intent(context, GuardRankActivity::class.java)
            intent.putExtra(IntentKeyConfig.ID, userId)
            context.startActivity(intent)
        }
    }
}
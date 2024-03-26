package cn.huanyuan.happymeet.ui.userinfo

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.TextView
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.RankInfo
import cn.huanyuan.happymeet.databinding.ActivityGuardRankBinding
import cn.huanyuan.happymeet.ui.userinfo.adapter.GuardRankAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.manager.ImChatManager
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:守护榜
 */
class GuardRankActivity : BaseActivity<ActivityGuardRankBinding, UserViewModel>(
    R.layout.activity_guard_rank,
    UserViewModel::class.java
) {
    private val guardRankAdapter by lazy { GuardRankAdapter() }
    private var userId: String = ""
    override fun initData() {
        setFullScreenStatusBar()
        userId = intent.getStringExtra(IntentKeyConfig.ID).toString()
        addEmptyView()
        mBinding.rvRank.adapter = guardRankAdapter
        requestData()
    }

    private fun addEmptyView() {
        val emptyView =
            LayoutInflater.from(mContext).inflate(R.layout.empty_guard_rank, null, false)
        val tvGuard = emptyView.findViewById<TextView>(R.id.tv_guard)
        tvGuard.setOnSingleClickListener {
            ImChatManager.toImChatPage(mContext,userId)
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
        }
        guardRankAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            intentToDetail(item!!)
        }
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
                val rankingList = it.rankList
                if (rankingList.size > 0) {
                    guardRankAdapter.isStateViewEnable = false
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
                }else{
                    guardRankAdapter.isStateViewEnable = true
                    mBinding.rankInfo1 = it.noGuardInfo
                }
            })
        }
    }

    companion object{
        fun lunch(context: Context,userId:String){
            val intent = Intent(context,GuardRankActivity::class.java)
            intent.putExtra(IntentKeyConfig.ID,userId)
            context.startActivity(intent)
        }
    }
}
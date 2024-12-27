package cn.yanhu.agora.ui.statics

import android.content.Context
import android.content.Intent
import android.view.View
import cn.yanhu.agora.databinding.ActivityLiveIncomeDetailBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.statistic.LiveIncomeDetailAdapter
import cn.yanhu.agora.bean.LiveIncomeDetailInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class LiveIncomeDetailActivity :
    BaseActivity<ActivityLiveIncomeDetailBinding, LiveStatisticsViewModel>(
        R.layout.activity_live_income_detail,
        LiveStatisticsViewModel::class.java
    ) {
    var roomId: String = ""
    private val incomeAdapter by lazy { LiveIncomeDetailAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        roomId = intent.getStringExtra(IntentKeyConfig.ROOM_ID).toString()
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无收入")
        incomeAdapter.stateView = emptyView
        mBinding.rvIncome.adapter = incomeAdapter
        incomeAdapter.addOnItemChildClickListener(R.id.avatarView,
            object : BaseQuickAdapter.OnItemChildClickListener<LiveIncomeDetailInfo.IncomeInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<LiveIncomeDetailInfo.IncomeInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = incomeAdapter.getItem(position) ?: return
                    RouteIntent.lunchPersonHomePage(item.userId)
                }

            })
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRoomIncomeDetail(roomId)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.incomeDetailLivedata.observe(this) { it ->
            parseState(it, {
                mBinding.incomeInfo = it
                incomeAdapter.isStateViewEnable = it.list.size <= 0
                incomeAdapter.submitList(it.list)
                mBinding.executePendingBindings()
            })
        }
    }

    companion object {
        fun lunch(context: Context, roomId: String) {
            val intent = Intent(context, LiveIncomeDetailActivity::class.java)
            intent.putExtra(IntentKeyConfig.ROOM_ID, roomId)
            context.startActivity(intent)
        }
    }
}
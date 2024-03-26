package cn.huanyuan.happymeet.ui.task

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityTaskCenterBinding
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.zj.netrequest.ext.parseState
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class TaskCenterActivity : BaseActivity<ActivityTaskCenterBinding, TaskViewModel>(
    R.layout.activity_task_center,
    TaskViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar(true)
        initTabLayout()
        initVpData()
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getTaskList()
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

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.taskInfoObservable.observe(this) { it ->
            parseState(it, {
                val fragment = frgList[0] as TaskItemFrg
                fragment.setTaskData(it.dailyTasks)
                val fragment2 = frgList[1] as TaskItemFrg
                fragment2.setTaskData(it.newbieTasks)
            })
        }
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("今日任务", "新人任务")
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.vpTask,
            list.toTypedArray(),
            textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_16).toFloat(),
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8)
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.vpTask)
    }

    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initVpData() {
        frgList.add(TaskItemFrg.newsInstance())

        frgList.add(TaskItemFrg.newsInstance())

        mBinding.vpTask.adapter = MyFragmentStateAdapter(mContext, frgList)
        mBinding.vpTask.offscreenPageLimit = frgList.size
    }

    companion object {
        fun lunch(mContext: FragmentActivity) {
            mContext.startActivity(Intent(mContext,TaskCenterActivity::class.java))
        }
    }
}
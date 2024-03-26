package cn.huanyuan.happymeet.ui.task

import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgTaskItemBinding
import cn.yanhu.baselib.base.BaseFragment

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class TaskItemFrg : BaseFragment<FrgTaskItemBinding, TaskViewModel>(
    R.layout.frg_task_item,
    TaskViewModel::class.java
) {

    private val taskItemAdapter by lazy { TaskItemAdapter() }
    override fun initData() {
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无新任务")
        taskItemAdapter.stateView = emptyView
        mBinding.rvTask.adapter = taskItemAdapter
    }

    fun setTaskData(taskList: MutableList<cn.huanyuan.happymeet.bean.TaskInfo>) {
        taskItemAdapter.submitList(taskList)
    }

    companion object {
        fun newsInstance(): TaskItemFrg {
            return TaskItemFrg()
        }
    }
}
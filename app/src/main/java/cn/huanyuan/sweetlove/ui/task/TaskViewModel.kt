package cn.huanyuan.sweetlove.ui.task

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.TaskResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class TaskViewModel : BaseViewModel() {

    val taskInfoObservable = MutableLiveData<ResultState<TaskResponse>>()

    fun getTaskList() {
        request({ rxApi.getTaskList() }, taskInfoObservable, false)
    }
}
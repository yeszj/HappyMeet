package cn.huanyuan.sweetlove.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
data class TaskInfo(
    val id: Int,
    val taskIcon: String,
    val title: String,
    val status: Int, //0:还没有完成 1:完成了但是没有领取 2:完成了并且领取了
    val redirectUrl: String,
    val rewardDesc:String,
    val buttonText:String,
):Serializable
package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/8/20
 * desc:
 */
data class CallFinishRewardBean(
    val seconds: Int,//通话时长 秒
    val chatType: String, //聊天类型 0语音 1视频
    val goldNum: String,//金币数量
    val videoCardGoldNum: String, //视频卡奖励金币
    val ifBalanceNoEnough:Boolean, //是否余额不足挂断
    val rewardUserId:String?
)
package cn.yanhu.imchat.bean.request

/**
 * @author: zhengjun
 * created: 2024/7/2
 * desc:
 */
data class RewardRequest(val chatUserId: String, var chatRewardInfos: MutableList<RewardInfo>) {
    data class RewardInfo(var msgId: String, var rewardGold: String, var msgType: Int)
}
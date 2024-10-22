package cn.yanhu.imchat.config

/**
 * @author: zhengjun
 * created: 2024/7/3
 * desc:
 */
object RechargeSourceConfig {
    const val SOURCE_RECHARGE_PAGE = 1 //充值页面
    const val SOURCE_PRIVATE_CHAT_FREE = 3 //私聊页充值-免费消息用完
    const val SOURCE_PRIVATE_CHAT_GIFT = 4 //私聊送礼充值
    const val SOURCE_PRIVATE_CHAT_VOICE_CALL = 5 //私聊语音呼叫充值
    const val SOURCE_PRIVATE_CHAT_VIDEO_CALL = 6 //私聊视频呼叫充值
    const val SOURCE_PRIVATE_CHAT_PIC = 7 //私聊发送图片
    const val SOURCE_PRIVATE_CHAT_EMOJI = 8 //私聊发送表情
    const val SOURCE_PRIVATE_CHAT_VIDEO = 9 //私聊发送视频消息
    const val SOURCE_PRIVATE_CHAT_TXT = 30 //私聊发送文字消息
    const val SOURCE_PRIVATE_CHAT_VOICE = 31 //私聊发送语音消息


    const val SOURCE_VIDEO_CALL_BALANCE= 10 //视频通话过程中余额不足
    const val SOURCE_VIDEO_CALL_GIFT= 11 //视频通话过程中送礼充值
    const val SOURCE_VOICE_CALL_BALANCE= 12 //语音通话过程中余额不足
    const val SOURCE_VOICE_CALL_GIFT= 13 //语音通话过程中余额不足
    const val SOURCE_CALL_VIDEO_AGREE_BALANCE = 14 //收到视频通话 接通时余额不足
    const val SOURCE_CALL_VOICE_AGREE_BALANCE = 15 //收到语音通话 接通时余额不足
    const val SOURCE_LIVE_ROOM_SEAT = 17 //直播间上麦
    const val SOURCE_LIVE_ROOM_RECHARGE = 18 //直播间点击充值
    const val SOURCE_LIVE_ROOM_GIFT = 19 //直播间送礼
    const val SOURCE_RANDOM_MATCH = 20 //随机缘分接电话余额不足
    const val SOURCE_ACCOST = 21 //搭讪余额不足
    const val SOURCE_APPLY_PRIVATE_ROOM = 22 //申请进入专属房间
    const val SOURCE_GROUP_CHAT = 23 //群聊页面充值
    const val SOURCE_GROUP_CHAT_SEND_RED_PACKET = 24 //群聊页面发红包
    const val SOURCE_NOBLE = 25 //开通贵族来源
    const val SOURCE_SALE_POP_PAGE_BACK = 26 //首次特价充值弹框-充值页面返回
    const val SOURCE_SALE_POP_NO_FREE = 27 //首次特价充值弹框-私聊免费消息用完

    const val SOURCE_STORE_BUY = 28 //装扮中心购买余额不足

    const val SOURCE_RELATIONSHIP_BIND = 40 //挚友墙绑定

}
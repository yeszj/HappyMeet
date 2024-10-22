package cn.yanhu.commonres.config

/**
 * @author: zhengjun
 * created: 2024/10/9
 * desc:
 */
object ChatConstant {
    const val CUSTOM_MSG = "custom_msg"
    const val MSG_ALERT = "msg_alert"

    const val SOURCE = "source"
    const val CUSTOM_DATA = "data"
    const val HAS_SEND_GIFT = "hasSendGift"
    const val SHOW_SEND_GIFT = "showSendGift"
    const val  ISSYSTEMRANDOM= "isSystemRandom"
    const  val MSG_ADD_FRIEND = "msg_add_friend" //自定义 添加好友
    const  val MSG_QIANXIAN = "msg_qianxian" //自定义 为爱牵线
    const  val MSG_CUSTOM_EMOJI = "msg_custom_emoji" //自定义小表情和文字混排消息


    const  val MSG_CUSTOM_GIF_EMOJI = "msg_custom_gif_emoji" //自定义动态表情消息
    const val CUSTOM_SEND_USER_INFO = "send_user_info"
    const  val MSG_INVITE_SEND_GIFT = "msg_invite_send_gift" //女方邀请对方赠送礼物成为好友消息
    const val CUSTOM_SEND_ATYPE = "sendType"


    //    /*
    //     * 一对一通话 end
    //     * */
    //
    //    String MSG_GIFT = "msg_gift";//自定义 礼物消息
    const  val MSG_GIFT = "msg_gift" //自定义 礼物消息

    const  val MSG_REWARD = "msg_reward" //自定义 亲密度礼物

    const  val MSG_PHONE = "msg_phone" //自定义 音视频通话
    const  val MSG_GROUP_NEW = "msg_group_new" //群聊 新用户入群消息
    const  val MSG_GROUP_RED_PACKET = "msg_group_red_packet" //群聊红包
    const val MSG_GROUP_CENTER_TIPS = "msg_group_center_tips"
    const val MSG_GROUP_INVITE = "msg_group_invite" //群聊邀请

    const  val WELCOME_CONTENT = "welcome_content"

    const  val ATE_USER_INFO = "ate_user_info"
    const  val MSG_BIND_RELATIONSHIP = "msg_bind_relationship" //绑定挚友关系


    var ACTION_MSG_CALL_ROSE_LACK_FINISH = 6 //一对一通话余额不足，结束通话

    var ACTION_MSG_CALL_ROSE_LACK_ALERT = 7 //一对一通话余额不足，提示男嘉宾


    var ACTION_PHONE_SEND_GIFT = 3006 //赠送礼物

    var ACTION_PHONE_CALL_VOICE = 3001 //一对一语音通话

    var ACTION_PHONE_CALL_VIDEO = 3002 //一对一视频通话

    var ACTION_PHONE_CALL_ANSWER = 3003 //一对一通话同意

    var ACTION_PHONE_CALL_REFUSE = 3004 //一对一通话取消

    var ACTION_PHONE_CALL_FINISH = 3005 //一对一通话结束

    var ACTION_PHONE_CALL_BALANCE_NOT_ENOUGH = 3008 //一对一通话对方余额不足


    const val ACTION_MSG_ROSE_LACK_ALERT = 5 //玫瑰数量不够，提示男嘉宾
    const val ACTION_MSG_ROSE_LACK_KICK_OUT = 4 //玫瑰数量不够，踢出房间
    var ACTION_MSG_UP_WHEET_TIMER = 11 //男嘉宾免费上麦剩余1分钟倒计时
    var OPERATE_LEAVE = 19 //强制离开

    const val ACTION_MSG_OPEN_AUTO_SEAT = 1003 //自动上麦开启
    const val ACTION_MSG_CLOSE_AUTO_SEAT = 1004 //自动下麦关闭
    const val ACTION_MSG_SWITCH_TYPE_PLAZA = 1010 //切换房间为大厅房间
    const val ACTION_MSG_SWITCH_MIKE = 1007 //开关麦

    const val ACTION_MSG_OPEN_MIKE_ASSIGN = 1008 //房主开指定嘉宾麦

    const val ACTION_MSG_CLOSE_MIKE_ASSIGN = 1009 //房主关指定嘉宾麦

    const val ACTION_MSG_APPLY_SET_UP = 1005 //申请上麦
    const val ACTION_MSG_SIT_DOWN = 1002 //强制下麦
    const val ACTION_MSG_SET_UP = 1001 //邀请上麦
    const val ACTION_MSG_APPLY_SET_UP_SUCCESS = 1006 //申请上麦成功
    const val ACTION_MSG_ADMIN_SIT_DOWN = 10021 //管理员强制下麦
    const val ACTION_MSG_ADMIN_ALERT = 10022 //管理员警告主持人
    const val ACTION_USER_OUT_TIME_LEAVE = 1011 //麦上用户离线超过1分钟 操作下麦



}
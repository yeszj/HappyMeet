package cn.yanhu.commonres.bean

import org.litepal.crud.LitePalSupport

/**
 * @author: zhengjun
 * created: 2024/5/17
 * desc:
 */
class AppMsgNotifyInfo : LitePalSupport() {
    var id: Long = 0
    var showTimeMills: Long = 0
    var msgType: Int = 0 //4直播 5好友上线
    var userId: String = ""
    var nickName: String = ""
    var portrait: String = ""
    var content: String = "" //正在直播中/刚刚上线了
    var userType: Int = 0 //0好友 1关注的人 2粉丝
    var roomId: String = ""//房间id 直播消息才有
    var relationshipType: Int = 0

    companion object {
        const val MSG_TYPE_READ_IM = 1 //查看IM消息
        const val MSG_TYPE_READ_HOMEPAGE = 2//查看主页
        const val MSG_TYPE_RECEIVE_IM = 3 //收到IM消息
        const val MSG_TYPE_USER_LIVE = 4 //直播
        const val MSG_TYPE_USER_ONLINE = 5 //好友上线
    }
}
package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: witness
 * created: 2022/12/15
 * desc:
 */
data class ChatRoomGiftMsg(
    val sendUser: BaseUserInfo,
    val receiverUser: BaseUserInfo=BaseUserInfo(),
    val giftInfo: GiftInfo = GiftInfo(),
    var currentIndex: Int = 0,
    var latestRefreshTime: Long = 0,//礼物刷新时间
    var giftStayTime: Long = 0,//礼物持续时间
    var giftCount:Int = 0,
    var source:Int = 1,
    var isSendNetGift : Boolean = true, //是否调用服务端接口发送礼物
    var toUid:MutableList<String> = mutableListOf()
) :
    GiftIdentify, Cloneable,Serializable {

    companion object{
        const val SOURCE_CHATROOM = 1 //语聊房送出
        const val SOURCE_VIDEO_MATCH = 2 //视频匹配房间送出
        const val SOURCE_VOICE_MATCH = 3 //语音匹配房间送出
        const val SOURCE_SINGLE_MATCH = 4 //单向配对送出
        const val SOURCE_SINGLE_VIDEO_MATCH = 5 //一对一视频送出
        const val SOURCE_SINGLE_VOICE_MATCH = 6 //一对一语音送出
        const val SOURCE_SINGLE_CHAT= 7 //私聊送出
    }

    override fun compareTo(other: GiftIdentify): Int {
        return (this.theLatestRefreshTime - other.theLatestRefreshTime).toInt()
    }

    override fun getTheGiftCount(): Int {
        return giftCount
    }

    override fun setTheGiftCount(count: Int) {
        giftCount = count
    }

    override fun getTheGiftStay(): Long {
        return giftStayTime
    }

    override fun setTheGiftStay(stay: Long) {
        giftStayTime = stay
    }

    override fun getTheLatestRefreshTime(): Long {
        return latestRefreshTime
    }

    override fun setTheLatestRefreshTime(time: Long) {
        latestRefreshTime = time
    }

    override fun getTheCurrentIndex(): Int {
        return currentIndex
    }

    override fun setTheCurrentIndex(index: Int) {
        currentIndex = index
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
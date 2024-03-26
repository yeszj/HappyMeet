package cn.yanhu.dynamic.bean

import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.AppCacheManager

/**
 * @author: zhengjun
 * created: 2023/6/30
 * desc:
 */
class DiscussInfo(
    val info: String,
    val commentTime: String,
    val isAuthorComment:Boolean,
    var firstLevelCommentReplayCount:Int,
    var commentIsLike:Boolean,
    var commentLikeCount:Int,
    val commentUser:BaseUserInfo?,
    val replayComments:MutableList<DiscussInfo>,
    var page:Int = 0,
    var isReply:Boolean,
    var commentId:String,//动态id 本地赋值的字段，用于页面显示逻辑
): BaseUserInfo() {
    fun isSelf():Boolean{
        return AppCacheManager.userId == userId
    }
}

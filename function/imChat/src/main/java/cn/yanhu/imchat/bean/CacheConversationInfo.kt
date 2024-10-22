package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/6/26
 * desc:
 */
data class CacheConversationInfo(
    val unReadMsgCount: Int,
    val conversationId: String,
    val cacheMessage: ConversationFinalMessageInfo
)
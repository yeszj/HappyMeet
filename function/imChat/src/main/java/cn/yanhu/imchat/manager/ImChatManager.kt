package cn.yanhu.imchat.manager

import android.content.Context
import cn.yanhu.imchat.ui.chat.ImChatActivity

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
object ImChatManager {

    fun toImChatPage(mContext:Context,userId:String){
        ImChatActivity.lunch(mContext,userId)
    }

    fun toImGroupChatPage(mContext:Context,groupId:String){
    }


    fun toBlackListPage(mContext:Context){
    }
}
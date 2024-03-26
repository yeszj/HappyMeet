package cn.yanhu.imchat.manager

import android.content.Context
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.netease.yunxin.kit.corekit.route.XKitRouter

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
object ImChatManager {

    fun toImChatPage(mContext:Context,userId:String){
        XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
            .withParam(RouterConstant.CHAT_ID_KRY, userId).withContext(mContext).navigate()
    }
}
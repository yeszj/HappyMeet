package cn.yanhu.imchat.manager

import android.content.Context
import com.netease.nimlib.sdk.team.model.Team
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant.CHAT_KRY
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_SEARCH_PAGE
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_MY_BLACK_PAGE
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

    fun toImGroupChatPage(mContext:Context,groupId:String){
        XKitRouter.withKey(RouterConstant.PATH_CHAT_TEAM_PAGE)
            .withParam(RouterConstant.CHAT_ID_KRY, groupId).withContext(mContext).navigate()
    }

    fun toGroupHistoryPage(mContext:Context,team:Team){
        XKitRouter.withKey(PATH_CHAT_SEARCH_PAGE)
            .withParam(CHAT_KRY, team)
            .withContext(mContext)
            .navigate()
    }

    fun toBlackListPage(mContext:Context){
        XKitRouter.withKey(PATH_MY_BLACK_PAGE)
            .withContext(mContext)
            .navigate()
    }
}
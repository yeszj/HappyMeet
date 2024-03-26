package cn.yanhu.imchat.bean

import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.SexManager


/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupDetailInfo: GroupBean() {
    var groupUserList:MutableList<GroupUserInfo> = mutableListOf()
    var manCount:Int = 0
    var womanCount:Int = 0

    fun isMan():Boolean{
        return SexManager.isMan(AppCacheManager.gender)
    }
}
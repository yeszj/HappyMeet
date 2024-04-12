package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupOnlineData {
    var totalCount: Int = 1
    val onlineUserList: MutableList<GroupUserInfo> = mutableListOf()
}
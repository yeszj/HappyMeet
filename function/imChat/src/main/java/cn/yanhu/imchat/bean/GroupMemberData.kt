package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupMemberData {
    var groupRole: Int = 1
    val groupUserList: MutableList<GroupUserInfo> = mutableListOf()
}
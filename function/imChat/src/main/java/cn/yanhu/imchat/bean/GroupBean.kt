package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
open class GroupBean {
    val groupIcon = ""
    val groupName = ""
    val groupId = ""
    val id = ""
    val groupNotice = ""
    val groupProvince = ""
    val groupUserCount = 0
    val peopleCountDesc = ""
    val visitorEnterRule = 0//游客能否进入 0不能进 1男性可进 2女性可进 3所有性别可进
    var groupRole: Int = 0 //1=群主；10=管理员； 30平台管理员； 50=普通用户；60=游客
    var onlineCount = 0
    fun isGroupUser(): Boolean {
        return isOwner() || isGroupAdmin() || groupRole == ROLE_GROUP_USER
    }

    fun isOwner(): Boolean {
        return groupRole == ROLE_OWNER
    }

    fun isGroupAdmin(): Boolean {
        return groupRole == ROLE_ADMIN
    }

    fun isVisitor(): Boolean {
        return groupRole == ROLE_GROUP_VISITOR
    }

    companion object {
        const val ROLE_OWNER = 1 //群主

        const val ROLE_ADMIN = 10 //群管理员

        const val ROLE_APP_ADMIN = 30 //超管 平台管理员

        const val ROLE_GROUP_USER = 50 //群成员

        const val ROLE_GROUP_VISITOR = 60 //游客访问

        const val VISITOR_PROHIBIT = 0
        const val VISITOR_MAN = 1
        const val VISITOR_WOMAN = 2
        const val VISITOR_ALL = 3

    }
}
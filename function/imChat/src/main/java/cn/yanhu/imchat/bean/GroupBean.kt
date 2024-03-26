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
    var groupRole:Int = 0 //1=群主；10=管理员；50=普通用户；60=游客
    fun isGroupUser():Boolean{
        return isOwner() || isGroupAdmin() || groupRole== ROLE_GROUP_USER
    }

    fun isOwner():Boolean{
        return groupRole == ROLE_OWNER
    }

    fun isGroupAdmin():Boolean{
        return groupRole == ROLE_ADMIN
    }

    companion object{
        const val  ROLE_OWNER = 1 //群主

        const val ROLE_ADMIN = 10 //群管理员

        const val ROLE_APP_ADMIN = 30 //超管 平台管理员

        const val ROLE_GROUP_USER = 50 //群成员

        const val ROLE_GROUP_VISITOR = 60 //游客访问

    }
}
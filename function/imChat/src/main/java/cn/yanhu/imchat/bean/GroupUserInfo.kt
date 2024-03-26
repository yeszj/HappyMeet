package cn.yanhu.imchat.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupUserInfo : BaseUserInfo() {
     var groupRole = 0 //1=群主；10=管理员；50=普通用户；60=游客
     var longClick = false
     fun isOwner():Boolean{
          return groupRole == GroupBean.ROLE_OWNER
     }

     fun isGroupAdmin():Boolean{
          return groupRole == GroupBean.ROLE_ADMIN
     }

     fun isVisitor():Boolean{
          return groupRole == GroupBean.ROLE_GROUP_VISITOR
     }
}
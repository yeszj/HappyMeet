package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
class ChatUserInfo{
     var intimate = 0.0 //亲密度
     var isAuth = false
     var onlineStatus = 0

     fun isOnline():Boolean{
          return onlineStatus == 0
     }
}
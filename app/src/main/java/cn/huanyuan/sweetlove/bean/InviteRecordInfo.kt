package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
class InviteRecordInfo : BaseUserInfo() {
    var isAuth: Boolean = false
    var totalIncome: String = ""
    var isRealNameAuth:Boolean = false
    var inviteTime:String = ""
    var contributionIncome:String = ""
}
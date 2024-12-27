package cn.yanhu.agora.listener

import cn.yanhu.commonres.bean.UserDetailInfo

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
interface OnSendSeatInviteListener {
    fun onSendInvite(map:MutableMap<String,Any>,userInfo: UserDetailInfo)
}
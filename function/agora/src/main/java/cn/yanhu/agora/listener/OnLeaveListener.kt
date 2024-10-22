package cn.yanhu.agora.listener

import cn.yanhu.commonres.bean.ChatCallResponseInfo


/**
 * @author: zhengjun
 * created: 2024/4/30
 * desc:
 */
interface OnLeaveListener {
    fun onLeave(response: ChatCallResponseInfo? )
}
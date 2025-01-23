package cn.yanhu.commonres.manager

import cn.zj.netrequest.application.ApplicationProxy

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
object WebUrlManager {
    const val USER_AGREEMENT = "https://thread.tcjhz.com/agreement/sweetlove-zc.html"
    const val PRIVACY_AGREEMENT = "https://thread.tcjhz.com/agreement/sweetlove-ys.html"
    const val PHONE_AGREEMENT = "https://thread.tcjhz.com/agreement/yys-shouquan.html"

    const val LIVE_ROOM_AGREEMENT = "https://thread.tcjhz.com/agreement/sweetlove-hn.html"

    const val RECHARGE_AGREEMENT = "https://thread.tcjhz.com/agreement/sweetlove-cz.html"

    const val WITHDRAW_AGREEMENT = "https://thread.tcjhz.com/agreement/sweetlove-tx.html"

    val LOG_OFF = ApplicationProxy.instance.getServeAddress() + "destoryUser"

    val SECURITY_AGREEMENT = ApplicationProxy.instance.getServeAddress() + "safe/reminder"

    const val ANGLE_ROOM_RULE = "https://video.chanyinet.com/agreement/angel_room_rules.html"

    const val  SONG_ROOM_RULE = "https://video.chanyinet.com/agreement/k_room_rules.html"

}
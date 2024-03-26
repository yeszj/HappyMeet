package cn.yanhu.commonres.manager

import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
object LiveDataEventManager {
    const val SWITCH_MAIN_TAB = "switchMainTab"
    const  val SWITCH_TO_SAME_CITY = "switch_to_same_city"
    const val LIKE_MOMENT_SUCCESS = "like_moment_success"

    const val UNLIKE_MOMENT_SUCCESS = "unlike_moment_success"

    const val CHAT_GROUP_MSG = "ChatGroupMsg"

    const val SINGLE_INTIMATE_MSG = "single_intimate_msg"

    const val START_FILTER_USER = "startFilterUser"

    const val DISCUSS_SUCCESS = "discussSuccess"

    const val DELETE_DISCUSS_SUCCESS = "deleteDiscussSuccess"

    const val DELETE_MOMENT_SUCCESS = "deleteMomentSuccess"

    const val PAY_RESULT = "pay_result"

    const val WX_AUTH_SUCCESS = "wx_auth_success"

    const val BIND_CODE_SUCCESS = "bind_code_success"

    const val ROSE_BALANCE_CHANGE = "rose_balance_change"

    const val DRESS_BUY_SUCCESS = "dress_buy_success"

    const val DRESS_UP_SUCCESS = "dress_up_success"
    @JvmStatic
    fun sendLiveDataMessage(key: String, value: Any = key) {
        LiveEventBus.get<Any>(key).post(value)
    }
}
package cn.yanhu.agora.ui.imphone

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.CheckCallBalanceRes
import cn.yanhu.agora.listener.OnLeaveListener
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/10/15
 * desc:
 */
class ImPhoneViewModel: BaseViewModel()  {
    val chatCallObserver = MutableLiveData<ResultState<ChatCallResponseInfo>>()
    val checkCallBalanceObserver = MutableLiveData<ResultState<CheckCallBalanceRes>>()
    val getTokenObserver = MutableLiveData<ResultState<String>>()

    fun getAgoraToken(callId: String) {
        request({ agoraRxApi.getAgoraToken(callId) }, getTokenObserver)
    }

    fun checkCallBalance(callId: String) {
        request({ agoraRxApi.checkCallBalance(callId) }, checkCallBalanceObserver)
    }

    fun deduction(callId: String) {
        request2({ agoraRxApi.deduction(callId) }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {

            }
        })
    }


    fun call(chatUserId: String, chatType: String, status: String, uid: String) {
        val hashMap = HashMap<String, String>()
        hashMap["chatUserId"] = chatUserId
        hashMap["chatType"] = chatType
        hashMap["status"] = status
        if (status != "0") {
            hashMap["uid"] = uid
        }
        request2({ agoraRxApi.call(hashMap) }, object : OnRequestResultListener<ChatCallResponseInfo> {
            override fun onSuccess(data: BaseBean<ChatCallResponseInfo>) {

            }
        })
    }

    fun startCall(chatUserId: String, chatType: String, status: String, uid: String) {
        val hashMap = HashMap<String, String>()
        hashMap["chatUserId"] = chatUserId
        hashMap["chatType"] = chatType
        hashMap["status"] = status
        if (status != "0") {
            hashMap["uid"] = uid
        }
        request({ agoraRxApi.call(hashMap) }, chatCallObserver)
    }

    @SuppressLint("CheckResult")
    fun finishCall(
        chatUserId: String,
        chatType: String,
        status: String,
        uid: String,
        second: Int,
        onLeaveListener: OnLeaveListener
    ) {
        val hashMap = java.util.HashMap<String, String>()
        hashMap["chatUserId"] = chatUserId
        hashMap["chatType"] = chatType
        hashMap["status"] = status
        hashMap["endSecondsTime"] = second.toString()
        if (status != "0") {
            hashMap["uid"] = uid
        }
        request2({ agoraRxApi.call(hashMap) }, object : OnRequestResultListener<ChatCallResponseInfo> {
            override fun onSuccess(data: BaseBean<ChatCallResponseInfo>) {
                onLeaveListener.onLeave(data.data)
            }
            override fun onFail(code: Int?, msg: String?) {
                onLeaveListener.onLeave(null)
            }
        })
    }

}
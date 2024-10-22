package cn.yanhu.imchat.manager

import android.Manifest
import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.google.gson.Gson

/**
 * @author: zhengjun
 * created: 2024/7/4
 * desc:
 */
object ImCallManager {
    const val CALL_VIDEO = 1
    const val CALL_VOICE = 0

    /**
     * 发起视频/语音通话
     * chatType 1视频 0语音
     */
    @JvmStatic
    fun checkCall(activity: FragmentActivity, chatType: Int, toUserId: String) {
        if (chatType == CALL_VOICE) {
            voiceRequestPermission(activity, toUserId)
        } else {
            videoRequestPermission(activity, toUserId)
        }
    }


    private var onConnectListener: OnConnectListener? = null

    @JvmStatic
    fun checkCall(
        activity: FragmentActivity,
        chatType: Int,
        toUserId: String,
        onConnectListener: OnConnectListener?
    ) {
        ImCallManager.onConnectListener = onConnectListener
        if (chatType == CALL_VOICE) {
            voiceRequestPermission(activity, toUserId)
        } else {
            videoRequestPermission(activity, toUserId)
        }
    }


    /*
     * 权限申请
     * */
    private fun voiceRequestPermission(
        activity: FragmentActivity,
        toUserId: String,
    ) {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            activity,
            permissions,
            "对爱交友想访问您的麦克风权限，用于语音通话",
            "您拒绝授权麦克风权限，无法使用语音通话功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    startCall(activity, 0, toUserId)
                }

                override fun onFail() {}
            })
    }

    private fun videoRequestPermission(
        activity: FragmentActivity,
        toUserId: String,
    ) {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            activity,
            permissions,
            "对爱交友想访问您的以下权限，用于视频通话",
            "您拒绝授权相关权限，无法使用视频通话功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    startCall(activity, 1, toUserId)
                }

                override fun onFail() {}
            })
    }

    private fun startCall(
        activity: FragmentActivity,
        chatType: Int,
        toUserId: String
    ) {
        if (ApplicationProxy.instance.isMiniLiveRoomShow() || ApplicationProxy.instance.getLiveRoomActivity() != null) {
            CutLiveRoomUtils.showChangeAlert(
                object : CutLiveRoomUtils.ChangeListener {
                    override fun sure() {
                        requestCall(
                            activity,
                            chatType, toUserId
                        )
                    }
                },
                if (chatType == 0) "你确定要离开当前房间并进入语音通话吗？" else "你确定要离开当前房间并进入视频通话吗？"
            )
        } else {
            requestCall(activity, chatType, toUserId)
        }
    }


    private var hasStartCall = false

    /*
     * 申请一对一通话
     * */
    @SuppressLint("CheckResult")
    private fun requestCall(
        activity: FragmentActivity,
        chatType: Int,
        toUserId: String,
    ) {
        if (!ApplicationProxy.instance.hasLoadAgoraSdk()) {
            LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRES,true)
            return
        }
        if (hasStartCall) {
            return
        }
        hasStartCall = true
        val hashMap = mutableMapOf<String, String>()
        hashMap["chatUserId"] = toUserId
        hashMap["chatType"] = chatType.toString()
        hashMap["status"] = ChatCallStatusConfig.STATUS_SEND_CALL
        request({ imChatRxApi.call(hashMap)},object : OnRequestResultListener<ChatCallResponseInfo>{
            override fun onSuccess(data: BaseBean<ChatCallResponseInfo>) {
                onConnectListener?.onOnConnectSuccess()

                val chatCallInfo = data.data
                chatCallInfo?.chatType = chatType
                val map: MutableMap<String, Any> =
                    HashMap()
                val callInfo = Gson().toJson(chatCallInfo)
                RouteIntent.toFromWaitPhoneActivity(callInfo)
                map["callInfo"] = callInfo
                EmMsgManager.sendCmdMessagePeople(
                    toUserId,
                    if (chatType == 0) ChatConstant.ACTION_PHONE_CALL_VOICE else ChatConstant.ACTION_PHONE_CALL_VIDEO,
                    map
                )
                hasStartCall = false
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                hasStartCall = false
                if (code==ErrorCode.CODE_NO_BALANCE){
                    showRechargeListDialog(activity)
                }else{
                    showToast(msg)
                }
            }

        })
    }

    private fun showRechargeListDialog( activity: FragmentActivity) {
       ApplicationProxy.instance.showRechargePop(activity,true)
    }

    interface OnConnectListener {
        fun onOnConnectSuccess()
    }
}
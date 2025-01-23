package cn.yanhu.commonres.router

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.utils.PermissionXUtils
import com.alibaba.android.arouter.launcher.ARouter

/**
 * @author: witness
 * created: 2022/12/5
 * desc:
 */
object RouteIntent {

    /*主module下的跳转配置start*/
    fun lunchToMain() {
        ARouter.getInstance().build(RouterPath.ROUTER_MAIN)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).navigation()
    }

    fun lunchToWebView(url: String?) {
        if(TextUtils.isEmpty(url)){
            return
        }
        val bundle = Bundle()
        bundle.putString("url", url)
        ARouter.getInstance().build(RouterPath.ROUTER_WEBVIEW).with(bundle).navigation()
    }

    fun lunchPersonHomePage(userId: String?) {
        if (TextUtils.isEmpty(userId)) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_USER_HOME_PAGE)
            .withString(IntentKeyConfig.ID, userId).navigation()
    }

    fun lunchGuardRankPage(userId: String?) {
        if (TextUtils.isEmpty(userId)) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_GUARD_RANK)
            .withString(IntentKeyConfig.ID, userId).navigation()
    }


    fun lunchPersonHomePage(userInfo: SameCityUserInfo?) {
        if (userInfo == null) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_USER_HOME_PAGE)
            .withSerializable(IntentKeyConfig.DATA, userInfo).navigation()
    }

    fun lunchToMyInviteRecord(){
        ARouter.getInstance().build(RouterPath.ROUTER_MY_INVITE_RECORD_PAGE).navigation()
    }

    //登录页面
    fun lunchLoginPage() {
        ARouter.getInstance().build(RouterPath.ROUTER_LOGIN).navigation()
    }

    fun lunchSystemMsgPage() {
        ARouter.getInstance().build(RouterPath.ROUTER_SYSTEMMESSAGE).navigation()
    }

    fun lunchRoseRecharge() {
        ARouter.getInstance().build(RouterPath.ROUTER_ROSE_RECHARGE).navigation()
    }

    fun lunchReportPage(userId:String? = null) {
        ARouter.getInstance().build(RouterPath.ROUTER_REPORT)
            .withString(IntentKeyConfig.ID,userId).navigation()
    }

    fun lunchSeenMeHistory() {
        ARouter.getInstance().build(RouterPath.ROUTER_SEEN_ME).navigation()
    }/*主module下的跳转配置end*/

    /*imChat module下的跳转配置start*/
    fun lunchGroupDetail(groupId: String?) {
        if (TextUtils.isEmpty(groupId)) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_GROUP_DETAIL)
            .withString(IntentKeyConfig.GROUP_ID, groupId).navigation()
    }/*imChat module下的跳转配置end*/

    /*dynamic module下的跳转配置start*/
    fun lunchPubDynamic() {
        ARouter.getInstance().build(RouterPath.ROUTER_PUB_DYNAMIC).navigation()
    }/*dynamic module下的跳转配置end*/


    /*agora module下的跳转配置start*/
    fun lunchToCreateRoom(roomConfigInfo: String) {

        ARouter.getInstance().build(RouterPath.ROUTER_CREATE_ROOM)
            .withString(IntentKeyConfig.DATA, roomConfigInfo).navigation()
    }


    fun lunchToBeautifulFace() {
        ARouter.getInstance().build(RouterPath.ROUTER_BEAUTIFUL_FACE).navigation()
    }

    fun lunchToRealNamPage(){
        ARouter.getInstance().build(RouterPath.ROUTER_REAL_NAME).navigation()
    }

    fun lunchToLiveRoom(activity: FragmentActivity,roomInfo:RoomDetailInfo) {
        val arrayList = ArrayList<String>()
        arrayList.add(Manifest.permission.CAMERA)
        arrayList.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(activity,arrayList,"为了便于使用上麦、实名认证等服务，请先同意麦克风、声音、摄像头权限",
            "您拒绝授权相关权限，无法使用部分功能",object : PermissionXUtils.PermissionListener{
                override fun onSuccess() {
                    ARouter.getInstance().build(RouterPath.ROUTER_LIVE_ROOM)
                        .withSerializable(IntentKeyConfig.DATA, roomInfo).navigation()
                }
                override fun onFail() {
                }

            })
    }


    fun toFromWaitPhoneActivity(callInfo: String?) {
        ARouter.getInstance().build(RouterPath.ROUTER_FROM_WAITPHONE)
            .withString(IntentKeyConfig.DATA, callInfo).navigation()
    }

    fun toToWaitPhoneActivity(callInfo: String?) {
        ARouter.getInstance().build(RouterPath.ROUTER_TO_WAITPHONE)
            .withString(IntentKeyConfig.DATA, callInfo).navigation()
    }

    fun toVideoPhonePage(chatUser: ChatCallResponseInfo) {
        ARouter.getInstance().build(RouterPath.ROUTER_VIDEO_PHONE)
            .withSerializable(IntentKeyConfig.DATA, chatUser).navigation()
    }


    /*agora module下的跳转配置end*/
}
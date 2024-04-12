package cn.yanhu.commonres.router

import android.os.Bundle
import android.text.TextUtils
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.GsonUtils

/**
 * @author: witness
 * created: 2022/12/5
 * desc:
 */
object RouteIntent {

    /*主module下的跳转配置start*/
    fun lunchToMain() {
        ARouter.getInstance().build(RouterPath.ROUTER_MAIN)
            .navigation()
    }

    fun lunchToWebView(url: String) {
        val bundle = Bundle()
        bundle.putString("url", url)
        ARouter.getInstance().build(RouterPath.ROUTER_WEBVIEW)
            .with(bundle)
            .navigation()
    }

    fun lunchPersonHomePage(userId: String?) {
        if (TextUtils.isEmpty(userId)) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_USER_HOME_PAGE)
            .withString(IntentKeyConfig.ID, userId)
            .navigation()
    }

    //登录页面
    fun lunchLoginPage() {
        ARouter.getInstance().build(RouterPath.ROUTER_LOGIN).navigation()
    }

    fun lunchRoseRecharge() {
        ARouter.getInstance().build(RouterPath.ROUTER_ROSE_RECHARGE).navigation()
    }

    fun lunchReportPage() {
        ARouter.getInstance().build(RouterPath.ROUTER_REPORT).navigation()
    }

    fun lunchSeenMeHistory() {
        ARouter.getInstance().build(RouterPath.ROUTER_SEEN_ME).navigation()
    }
    /*主module下的跳转配置end*/

    /*imChat module下的跳转配置start*/
    fun lunchGroupDetail(groupId: String?) {
        if (TextUtils.isEmpty(groupId)) {
            return
        }
        ARouter.getInstance().build(RouterPath.ROUTER_GROUP_DETAIL)
            .withString(IntentKeyConfig.GROUP_ID, groupId)
            .navigation()
    }
    /*imChat module下的跳转配置end*/

    /*dynamic module下的跳转配置start*/
    fun lunchPubDynamic() {
        ARouter.getInstance().build(RouterPath.ROUTER_PUB_DYNAMIC).navigation()
    }
    /*dynamic module下的跳转配置end*/


    /*agora module下的跳转配置start*/
    fun lunchToCreateRoom() {
        ARouter.getInstance().build(RouterPath.ROUTER_CREATE_ROOM).navigation()
    }


    fun lunchToBeautifulFace() {
        ARouter.getInstance().build(RouterPath.ROUTER_BEAUTIFUL_FACE).navigation()
    }

    fun lunchToLiveRoom(roomType: Int, roomId: String) {
        val roomList: MutableList<RoomListBean> = mutableListOf()
        val roomListBean = RoomListBean()
        roomListBean.roomId = roomId
        roomListBean.roomType = roomType
        roomList.add(roomListBean)
        ARouter.getInstance().build(RouterPath.ROUTER_LIVE_ROOM)
            .withString(IntentKeyConfig.DATA, GsonUtils.toJson(roomList))
            .navigation()
    }

    fun lunchToLiveRoom(roomList: MutableList<RoomListBean>,page:Int,roomId: String) {
        ARouter.getInstance().build(RouterPath.ROUTER_LIVE_ROOM)
            .withString( IntentKeyConfig.DATA, GsonUtils.toJson(roomList))
            .withString(IntentKeyConfig.ROOM_ID,roomId)
            .withInt(IntentKeyConfig.PAGE,page)
            .navigation()
    }


    /*agora module下的跳转配置end*/
}
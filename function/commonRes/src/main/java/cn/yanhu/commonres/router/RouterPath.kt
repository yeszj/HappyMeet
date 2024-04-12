package cn.yanhu.commonres.router

/**
 * @author: witness
 * created: 2022/11/21
 * desc:
 */
object RouterPath {

    /*主module下的跳转配置start*/
    //主页
    const val ROUTER_MAIN = "/app/main"
    // 登录页
    const val ROUTER_LOGIN = "/app/login"
    //谁看过我
    const val ROUTER_SEEN_ME = "/app/seenMe"

    const val ROUTER_REPORT= "/app/report"

    const val ROUTER_ROSE_RECHARGE = "/app/roseRecharge"

    const val ROUTER_WEBVIEW = "/app/webview"
    //个人中心
    const val ROUTER_USER_HOME_PAGE= "/app/userHomePage"

    /*主module下的跳转配置end*/



    /*imChat module下的跳转配置start*/
    const val ROUTER_GROUP_DETAIL = "/imChat/groupDetail"

    /*imChat module下的跳转配置end*/


    /*dynamic module下的跳转配置start*/
    const val ROUTER_PUB_DYNAMIC= "/dynamic/pubDynamic"
    /*dynamic module下的跳转配置end*/



    /*agora module下的跳转配置start*/
    //创建房间
    const val ROUTER_CREATE_ROOM= "/agora/createRoom"

    //美颜
    const val ROUTER_BEAUTIFUL_FACE= "/agora/beautifulFace"

    //直播房
    const val ROUTER_LIVE_ROOM= "/agora/liveRoom"
    /*agora module下的跳转配置end*/
}
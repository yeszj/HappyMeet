package cn.yanhu.agora.miniwindow

import android.app.Activity
import android.content.Context
import android.content.Intent
import cn.yanhu.agora.ui.liveRoom.live.LiveRoomActivity
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ui.chat.ImChatActivity
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2023/5/4
 * desc:
 */
object MiniWindowManager {
    @JvmStatic
    fun switchLiveToMiniFloat(context: Context) {
        var frontActivity: Activity? = ActivityUtils.getTopActivity() ?: return
        if (frontActivity is LiveRoomActivity ||
            frontActivity is VideoPhoneActivity
        ) {
            frontActivity = getUpActivity()
        }
        if (frontActivity==null){
            RouteIntent.lunchToMain()
        }else{
            val intent = Intent(context, frontActivity.javaClass)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun switchLiveToFront(context: Context, activity: Activity) {
        val intent = Intent(
            context,
            activity.javaClass
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.putExtra("isFinish", false)
        context.startActivity(intent)
    }

    @JvmStatic
    fun switchLiveToFront(type: Int, isFinish: Boolean = false) {
        val topActivity = ActivityUtils.getTopActivity()
        val intent = Intent(
            topActivity,
             LiveRoomActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.putExtra("isFinish", isFinish)
        topActivity.startActivity(intent)
    }

    @JvmStatic
    fun switchVideoToFront(context: Context, isFinish: Boolean = false) {
        val intent = Intent(
            context,
            VideoPhoneActivity::class.java
        )
        intent.putExtra("isFinish", isFinish)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        context.startActivity(intent)
    }

    @JvmStatic
    fun switchPageToFront(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        context.startActivity(intent)
    }

    @JvmStatic
    fun switchVoiceToFront(context: Context) {
//        val intent = Intent(
//            context,
//            VoicePhoneActivity::class.java
//        )
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//        context.startActivity(intent)
    }

    fun getUpActivity(): Activity? {
        val activityList: List<Activity> = ActivityUtils.getActivityList()
        return if (activityList.size > 1) {
            activityList[1]
        } else {
            null
        }
    }

    fun getChatActivity(): Activity? {
        val activityList: MutableList<Activity> = ActivityUtils.getActivityList().toMutableList()
        activityList.removeAt(0)
        if (activityList.size > 0) {
            activityList.forEach {
                if (it is ImChatActivity) {
                    return it
                }
            }
            return null
        } else {
            return null
        }
    }

    fun finishChatActivity() {
        val activityList: MutableList<Activity> = ActivityUtils.getActivityList().toMutableList()
        if (activityList.size > 0) {
            activityList.forEach {
                if (it is ImChatActivity) {
                    it.finish()
                }
            }
        }
    }
}
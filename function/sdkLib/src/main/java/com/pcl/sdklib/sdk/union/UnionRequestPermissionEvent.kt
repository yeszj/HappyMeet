package com.pcl.sdklib.sdk.union

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.text.TextUtils
import cn.yanhu.baselib.utils.DialogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.qiyukf.unicorn.api.event.EventCallback
import com.qiyukf.unicorn.api.event.UnicornEventBase
import com.qiyukf.unicorn.api.event.entry.RequestPermissionEventEntry

/**
 * Created by zhanyage on 2020/10/14
 * Describe:
 */
class UnionRequestPermissionEvent() :
    UnicornEventBase<RequestPermissionEventEntry> {
    private val h5MessageHandlerMap: MutableMap<String, String> = HashMap()

    init {
        h5MessageHandlerMap["android.permission.RECORD_AUDIO"] = "麦克风"
        h5MessageHandlerMap["android.permission.CAMERA"] = "相机"
        h5MessageHandlerMap["android.permission.READ_EXTERNAL_STORAGE"] = "存储"
        h5MessageHandlerMap["android.permission.WRITE_EXTERNAL_STORAGE"] = "存储"
        h5MessageHandlerMap["android.permission.READ_MEDIA_AUDIO"] = "多媒体文件"
        h5MessageHandlerMap["android.permission.READ_MEDIA_IMAGES"] = "多媒体文件"
        h5MessageHandlerMap["android.permission.READ_MEDIA_VIDEO"] = "多媒体文件"
        h5MessageHandlerMap["android.permission.POST_NOTIFICATIONS"] = "通知栏权限"
    }

    private fun transToPermissionStr(permissionList: List<String>?): String {
        if (permissionList.isNullOrEmpty()) {
            return ""
        }
        val set = HashSet<String?>()
        for (i in permissionList.indices) {
            if (!TextUtils.isEmpty(h5MessageHandlerMap[permissionList[i]])) {
                set.add(h5MessageHandlerMap[permissionList[i]])
            }
        }
        if (set.isEmpty()) {
            return ""
        }
        val str = StringBuilder()
        for (temp in set) {
            str.append(temp)
            str.append("、")
        }
        if (str.isNotEmpty()) {
            str.deleteCharAt(str.length - 1)
        }
        return str.toString()
    }

    /**
     * 该方法为点击相应的权限场景,用户可以通过RequestPermissionEventEntry.getPermissionList()拿到相应的权限,根据
     * 自己APP的权限规则,作自己的处理.
     *
     *
     * 比如判断客户之前点击的是拒绝权限还是不再询问,可以使用 AppCompatActivity.shouldShowRequestPermissionRationale()
     * 方法等各种情况都是在这个回调中进行自己的处理.
     *
     *
     * 各种情况都处理完了,可以告诉SDK,是要申请权限还是拒绝,调用SDK相应的方法.
     * callback.onProcessEventSuccess(requestPermissionEventEntry):用户同意授予权限
     * callback.onInterceptEvent():用户不授予权限，SDK自己处理不授予权限的提醒;或者用户自己处理不授予权限的提醒,就不要调用这个方法了
     *
     * @param requestPermissionEventEntry 获取权限相关的类
     * @param context                     当前界面的 context 对象，使用之前请判断是否为 null
     * @param callback                    sdk 的回调对象  注意：如果该事件 sdk 不需要回调的时候，这个对象会为 null，所以当使用的时候需要做一下非null判断
     */
    override fun onEvent(
        requestPermissionEventEntry: RequestPermissionEventEntry,
        context: Context,
        callback: EventCallback<RequestPermissionEventEntry>
    ) {

        //申请权限的场景
        //从本地选择媒体文件(视频和图片):0
        //拍摄视频场景:1
        //保存图片到本地:2
        //保存视频到本地:3
        //选择本地视频:4
        //选择本地文件:5
        //选择本地图片:6
        //拍照:7
        //录音:8
        //视频客服:9
        //通知栏权限:10
        if (context is Activity) {
            if (context.isFinishing) {
                return
            }
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                if ((context.baseContext as Activity).isFinishing) {
                    return
                }
            }
        }
        val type = requestPermissionEventEntry.scenesType
        if (type == 10) {
            //Toast.makeText(mApplicationContext, "适配Android13,没有通知栏权限,需要给通知栏权限", Toast.LENGTH_SHORT).show();
            return
        }
        val permissionName = transToPermissionStr(requestPermissionEventEntry.permissionList)
        val explainReasonStr =
            "为保证您功能的正常使用，" + "需要使用您的：" + (if (TextUtils.isEmpty(permissionName)) "相关" else permissionName) + "权限，拒绝不影响使用其他服务"
        DialogUtils.showConfirmDialog("权限申请", {
            callback.onProcessEventSuccess(requestPermissionEventEntry);
        }, {
            callback.onInterceptEvent();
        }, explainReasonStr, "拒绝", "同意"
        )
    }

    /**
     * 当相关权限被拒绝后,客户自己的处理
     * @return 返回值默认为false，若返回值为true，则客户自己处理
     */
    override fun onDenyEvent(
        context: Context, requestPermissionEventEntry: RequestPermissionEventEntry
    ): Boolean {
        val permissionName = transToPermissionStr(requestPermissionEventEntry.permissionList)
        val explainReasonStr =
            "您已经拒绝" + (if (TextUtils.isEmpty(permissionName)) "相关" else permissionName) + "权限，请前往设置中打开"
        try {
            PermissionUtils.launchAppDetailsSettings()
        } catch (e: Exception) {
            ToastUtils.showShort(explainReasonStr)
        }
        return true
    }
}
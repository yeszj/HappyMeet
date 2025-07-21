package cn.huanyuan.sweetlove.func.task

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.contains
import cn.huanyuan.sweetlove.bean.CutDeviceConsumeBean
import cn.huanyuan.sweetlove.bean.GlobalGiftBean
import cn.huanyuan.sweetlove.func.dialog.DeviceChangeAuthTipDialog
import cn.huanyuan.sweetlove.func.dialog.NewYearRedPacketPop
import cn.huanyuan.sweetlove.func.dialog.TeenTipPop
import cn.huanyuan.sweetlove.func.view.GlobalGiftLayout
import cn.huanyuan.sweetlove.func.view.GlobalUserOnlineLayout
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.userinfo.auth.RealNameActivity
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.CommonEventPopInfo
import cn.yanhu.commonres.bean.CommonTipsInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.pop.CommonImagePop
import cn.yanhu.commonres.pop.CommonTipDialog
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.task.AppPopTypeManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.pcl.sdklib.bean.CheckFaceAuthResult
import com.pcl.sdklib.bean.FaceAuthInfo


/**
 * @author: zhengjun
 * created: 2024/10/28
 * desc:
 */
class AppPopTask(val type: Int, val info: String) : BaseQueueTask() {
    companion object {
        var currentPopTask: AppPopTask? = null
    }

    override fun doTask() {
        currentPopTask = this
        val topActivity = ActivityUtils.getTopActivity()
        when (type) {
            AppPopTypeManager.TYPE_TEE_POP -> {
                showTeenTipPop(topActivity)
            }

            ChatConstant.GLOBAL_GIFT_ALERT -> {
                //礼物飘屏
                showGiftGlobalAnim(topActivity)
            }

            ChatConstant.ACTION_USER_ONLINE -> {
                //用户上线
                showUserOnlineGlobalAnim(topActivity)
            }

            ChatConstant.ACTION_FORCE_AUTH -> {
                showAuthTipPop(info.toInt(), topActivity)
            }

            ChatConstant.ACTION_NEW_YEAR_RED_PACKET -> {
                //新年红包
                showNewYearRedPacketPop(topActivity)
            }

            ChatConstant.ACTION_EVENT_POP -> {
                //活动弹框
                showEventPop(topActivity)
            }

            ChatConstant.ACTION_CHANGE_DEVICE -> {
                //设备变更
                showChangeDevicePop(topActivity)
            }
        }
    }

    //设备改变安全认证提示
    private var deviceChangeAuthTipDialog: DeviceChangeAuthTipDialog? = null
    private fun showChangeDevicePop(topActivity: Activity) {
        logcom("checkOaid","showChangeDevicePop")
        if (CommonUtils.isPopShow(deviceChangeAuthTipDialog)) {
            return
        }
        val cutDeviceConsumeBean = GsonUtils.fromJson(info, CutDeviceConsumeBean::class.java)
        deviceChangeAuthTipDialog = DeviceChangeAuthTipDialog.showPop(
            topActivity,
            object : DeviceChangeAuthTipDialog.OnClickAuthListener {
                override fun onAuth() {
                    if (cutDeviceConsumeBean.consumeGold <= cutDeviceConsumeBean.userGoldNum) {
                        toFaceAuth(cutDeviceConsumeBean, topActivity)
                    } else {
                        showToast("金币不足，请联系客服处理")
                    }
                }
            },
            dismissCallBack
        )
    }

    private var isLoadFaceInfo = false
    private fun toFaceAuth(cutDeviceConsumeBean: CutDeviceConsumeBean, topActivity: Activity) {
        if (isLoadFaceInfo) {
            return
        }
        isLoadFaceInfo = true
        request({ rxApi.getFaceAuthInfo() }, object : OnRequestResultListener<FaceAuthInfo> {
            override fun onSuccess(data: BaseBean<FaceAuthInfo>) {
                isLoadFaceInfo = false
                if (topActivity.isDestroyed) {
                    return
                }
                val faceInfo = data.data ?: return
                val checkFaceAuthResult = CheckFaceAuthResult(2, GsonUtils.toJson(faceInfo),true)
                RealNameActivity.lunch(topActivity, checkFaceAuthResult,2,cutDeviceConsumeBean.isIsConsume)
            }

            override fun onFail(code: Int?, msg: String?) {
                isLoadFaceInfo = false
            }

        }, true)
    }


    private var commonImagePop: CommonImagePop? = null
    private fun showEventPop(topActivity: Activity) {
        val fromJson =
            GsonUtils.fromJson(info, CommonEventPopInfo::class.java)

        GlideUtils.loadAsDrawable(
            topActivity,
            fromJson.bgImaUrl,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if (CommonUtils.isPopShow(commonImagePop)) {
                        return
                    }
                    commonImagePop =
                        CommonImagePop.showDialog(topActivity, fromJson, resource, dismissCallBack)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    doNextTask()
                }
            })
    }

    private var redPacketPop: NewYearRedPacketPop? = null
    private fun showNewYearRedPacketPop(topActivity: Activity) {
        GlideUtils.loadAsDrawable(topActivity, info, object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                if (CommonUtils.isPopShow(redPacketPop)) {
                    return
                }
                redPacketPop =
                    NewYearRedPacketPop.showDialog(topActivity, resource, dismissCallBack)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                doNextTask()
            }
        })
    }

    private var forceAuthTipDialog: CommonTipDialog? = null
    private fun showAuthTipPop(type: Int, topActivity: Activity) {
        if (CommonUtils.isPopShow(forceAuthTipDialog)) {
            return
        }
        val commonTipsInfo = CommonTipsInfo(
            "您的账户存在风险",
            "根据监管要求，需要尽快完善实名信息，避免影响您的后续使用。",
            "立即完善",
            type == 2,
            cn.yanhu.commonres.R.drawable.icon_secure_tip,
            type == 2
        )
        forceAuthTipDialog =
            CommonTipDialog.showDialog(
                topActivity,
                commonTipsInfo,
                object : CommonTipDialog.OnClickBtnListener {
                    override fun onClickCancel() {}
                    override fun onClickBtn() {
                        RouteIntent.lunchToRealNamPage()
                    }
                },
                object : SimpleCallback() {
                    override fun onDismiss(popupView: BasePopupView) {
                        super.onDismiss(popupView)
                        forceAuthTipDialog = null
                        doNextTask()
                    }
                })
    }


    private fun showUserOnlineGlobalAnim(topActivity: Activity?) {
        topActivity?.apply {
            val userInfo = Gson().fromJson(
                info, BaseUserInfo::class.java
            )
            val globalUserOnlineLayout = GlobalUserOnlineLayout(topActivity)
            val window: Window = topActivity.window
            val decorView = window.decorView as ViewGroup
            // 添加飘屏 View
            if (globalUserOnlineLayout.parent == null) {
                globalUserOnlineLayout.setUserInfo(userInfo)
                globalUserOnlineLayout.visibility = View.INVISIBLE
                decorView.addView(globalUserOnlineLayout)
                globalUserOnlineLayout.post {
                    val animatorSet: AnimatorSet = globalUserOnlineLayout.startAnimation()
                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            doNextTask()
                            if (globalUserOnlineLayout.parent != null && decorView.contains(
                                    globalUserOnlineLayout
                                ) && !topActivity.isDestroyed
                            ) {
                                decorView.removeView(globalUserOnlineLayout)
                            }
                        }
                    })
                    globalUserOnlineLayout.addOnAttachStateChangeListener(object :
                        OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            globalUserOnlineLayout.visibility = View.VISIBLE
                        }

                        override fun onViewDetachedFromWindow(v: View) {
                            globalUserOnlineLayout.removeOnAttachStateChangeListener(this)
                            AnimManager.removeAnimSet(animatorSet)
                            doNextTask()
                        }
                    })
                }
            }
        }
    }

    private fun showGiftGlobalAnim(topActivity: Activity?) {
        topActivity?.apply {
            val globalGiftBean = Gson().fromJson(
                info, GlobalGiftBean::class.java
            )
            val globalGiftLayout = GlobalGiftLayout(topActivity)
            val window: Window = topActivity.window
            val decorView = window.decorView as ViewGroup
            // 添加飘屏 View
            if (globalGiftLayout.parent == null) {
                globalGiftLayout.setModel(globalGiftBean)
                globalGiftLayout.visibility = View.INVISIBLE
                decorView.addView(globalGiftLayout)
                globalGiftLayout.post {
                    val animatorSet: AnimatorSet = globalGiftLayout.startAnimation()
                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            doNextTask()
                            if (globalGiftLayout.parent != null && decorView.contains(
                                    globalGiftLayout
                                ) && !topActivity.isDestroyed
                            ) {
                                decorView.removeView(globalGiftLayout)
                            }
                        }
                    })
                    globalGiftLayout.addOnAttachStateChangeListener(object :
                        OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            globalGiftLayout.visibility = View.VISIBLE
                        }

                        override fun onViewDetachedFromWindow(v: View) {
                            globalGiftLayout.removeOnAttachStateChangeListener(this)
                            AnimManager.removeAnimSet(animatorSet)
                            doNextTask()
                        }
                    })
                }

            }
            //GlobalGiftPop.showDialog(topActivity, globalGiftBean, dismissCallBack)
        }
    }

    private var teenTipPop: TeenTipPop? = null
    private fun showTeenTipPop(topActivity: Activity) {
        if (CommonUtils.isPopShow(teenTipPop)) {
            return
        }
        teenTipPop = TeenTipPop.showDialog(topActivity, dismissCallBack)
    }

    private val dismissCallBack = object : SimpleCallback() {
        override fun onDismiss(popupView: BasePopupView?) {
            super.onDismiss(popupView)
            doNextTask()
        }
    }
}
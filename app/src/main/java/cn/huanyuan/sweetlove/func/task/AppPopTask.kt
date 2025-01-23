package cn.huanyuan.sweetlove.func.task

import android.app.Activity
import android.graphics.drawable.Drawable
import cn.huanyuan.sweetlove.bean.GlobalGiftBean
import cn.huanyuan.sweetlove.func.dialog.GlobalGiftPop
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.config.ChatConstant
import cn.huanyuan.sweetlove.func.dialog.TeenTipPop
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.CommonTipsInfo
import cn.huanyuan.sweetlove.func.dialog.NewYearRedPacketPop
import cn.yanhu.commonres.bean.CommonEventPopInfo
import cn.yanhu.commonres.pop.CommonImagePop
import cn.yanhu.commonres.pop.CommonTipDialog
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.task.AppPopTypeManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

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
                showGiftGlobal(topActivity)
            }
            ChatConstant.ACTION_FORCE_AUTH ->{
                showAuthTipPop(info.toInt(),topActivity)
            }
            ChatConstant.ACTION_NEW_YEAR_RED_PACKET ->{
                //新年红包
                showNewYearRedPacketPop(topActivity)
            }
            ChatConstant.ACTION_EVENT_POP ->{
                //活动弹框
                showEventPop(topActivity)
            }
        }
    }

    private var commonImagePop:CommonImagePop?=null
    private fun showEventPop(topActivity: Activity) {
        val fromJson =
            GsonUtils.fromJson(info, CommonEventPopInfo::class.java)

        GlideUtils.loadAsDrawable(topActivity, fromJson.bgImaUrl, object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                if (CommonUtils.isPopShow(commonImagePop)){
                    return
                }
                commonImagePop = CommonImagePop.showDialog(topActivity, fromJson,resource,dismissCallBack)
            }
            override fun onLoadCleared(placeholder: Drawable?) {
            }
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                doNextTask()
            }
        })
    }

    private var redPacketPop: NewYearRedPacketPop?=null
    private fun showNewYearRedPacketPop(topActivity: Activity) {
        GlideUtils.loadAsDrawable(topActivity, info, object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                if (CommonUtils.isPopShow(redPacketPop)){
                    return
                }
                redPacketPop = NewYearRedPacketPop.showDialog(topActivity, resource, dismissCallBack)
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
            CommonTipDialog.showDialog(topActivity, commonTipsInfo, object : CommonTipDialog.OnClickBtnListener {
                override fun onClickCancel() {}
                override fun onClickBtn() {
                    RouteIntent.lunchToRealNamPage()
                }
            }, object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView) {
                    super.onDismiss(popupView)
                    forceAuthTipDialog = null
                    doNextTask()
                }
            })
    }

    private fun showGiftGlobal(topActivity: Activity?) {
        topActivity?.apply {
            val globalGiftBean = Gson().fromJson(
                info, GlobalGiftBean::class.java
            )
            GlobalGiftPop.showDialog(topActivity, globalGiftBean, dismissCallBack)
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
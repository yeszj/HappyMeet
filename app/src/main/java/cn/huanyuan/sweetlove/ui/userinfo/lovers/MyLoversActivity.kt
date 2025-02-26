package cn.huanyuan.sweetlove.ui.userinfo.lovers

import android.annotation.SuppressLint
import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivityMyLoversBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.func.dialog.BindLoversPop
import cn.yanhu.baselib.queue.TaskQueueManagerImpl
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.response.LoversResponse
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.task.GiftPopAnimTask
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.utils.TextUtils
import com.lxj.xpopup.core.BasePopupView

/**
 * @author: zhengjun
 * created: 2025/2/17
 * desc:
 */
@Route(path = RouterPath.ROUTER_MY_LOVERS)
class MyLoversActivity : BaseActivity<ActivityMyLoversBinding, LoversViewModel>(
    R.layout.activity_my_lovers,
    LoversViewModel::class.java
) {
    private var viewUserId: String = ""
    override fun initData() {
        setFullScreenStatusBar(true)
        val statusBarHeight = StatusBarUtil.getStatusBarHeight(mContext)
        ViewUtils.setMarginTop(mBinding.titleBar, statusBarHeight)
        viewUserId = intent.getStringExtra(IntentKeyConfig.ID).toString()
        mBinding.isSelf = AppCacheManager.userId == viewUserId
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getLoversIndex(viewUserId)
    }

    private var loversInfo: LoversResponse? = null

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.loversResponseObservable.observe(this) { it ->
            parseState(it, {
                loversInfo = it
                mBinding.loversRes = it
                mBinding.isSameSex = it.viewInfo.gender == AppCacheManager.gender
                bindTips(it)
            })
        }
        mViewModel.cancelLoversObservable.observe(this) { it ->
            parseState(it, {
                showToast("解除情侣成功～")
                DialogUtils.dismissLoading()
                EmMsgManager.sendCmdMessagePeople(loversInfo!!.viewInfo.userId,ChatConstant.ACTION_CANCEL_LOVERS,null)
                LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.REFRESH_USER_CACHE)
                requestData()
            }, {
                showOperateFail(it)
            })
        }

        mViewModel.bindLoversObservable.observe(this) { it ->
            parseState(it, {
                bindLoversSuccess()
            }, {
                showOperateFail(it)
            })
        }
    }

    private fun showOperateFail(it: CustomException) {
        DialogUtils.dismissLoading()
        if (it.code == ErrorCode.CODE_NO_BALANCE) {
            ApplicationProxy.instance.showRechargePop(mContext, true)
        } else {
            showToast(it.msg)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindTips(it: LoversResponse) {
        if ( it.loversType!=null && it.loversType!!>=1) {
            if (it.viewInfo.userId == AppCacheManager.userId) {
                mBinding.titleBar.setTitleRightText("解除情侣")
                mBinding.tvTips.visibility = View.VISIBLE
                mBinding.tvTips.text =
                    "你和${it.loverInfo!!.nickName}的情侣关系将在${it.remainDay}天后失效"
            } else if (it.loverInfo!!.userId == AppCacheManager.userId) {
                mBinding.titleBar.setTitleRightText("解除情侣")
                mBinding.tvTips.visibility = View.VISIBLE
                mBinding.tvTips.text =
                    "你和${it.viewInfo.nickName}的情侣关系将在${it.remainDay}天后失效"
            } else {
                mBinding.titleBar.setTitleRightText("")
                mBinding.tvTips.visibility = View.INVISIBLE
            }
        } else {
            mBinding.titleBar.setTitleRightText("")
            val userInfo = BaseUserInfo()
            userInfo.portrait =
                "https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/lovers_no_bind.png"
            it.loverInfo = userInfo
            mBinding.tvTips.visibility = View.INVISIBLE
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvBind.setOnSingleClickListener {
            showBindLoversPop()
        }
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                showCancelLoversPop()
            }
        })
    }

    private var bindLoversPop: BindLoversPop? = null
    private var sendGiftInfo:GiftInfo?=null
    private fun showBindLoversPop() {
        if (CommonUtils.isPopShow(bindLoversPop) || loversInfo == null) {
            return
        }
        bindLoversPop = BindLoversPop.showDialog(
            mContext,
            loversInfo!!.giftList,
            object : BindLoversPop.OnSendListener {
                override fun onSendGift(giftInfo: GiftInfo) {
                    sendGiftInfo = giftInfo
                    DialogUtils.showLoading()
                    mViewModel.bindLovers(viewUserId, giftInfo.id.toString())
                }
            })
    }

    private val giftAnimTaskManager: TaskQueueManagerImpl = TaskQueueManagerImpl()
    private fun showGiftSvgAnim() {
        if (sendGiftInfo == null || TextUtils.isEmpty(sendGiftInfo?.svga)) {
            return
        }
        giftAnimTaskManager.addTask(
            GiftPopAnimTask(
                sendGiftInfo!!, mBinding.svgGiftAnim, mBinding.videoGiftAnimView
            )
        )
    }

    private fun bindLoversSuccess() {
        showGiftSvgAnim()
        DialogUtils.dismissLoading()
        showToast("绑定情侣成功～")
        requestData()
        bindLoversPop?.dismiss()
        EmMsgManager.saveAlert(
            "恭喜，你与${loversInfo?.viewInfo?.nickName}已成为情侣！",
            "",
            "", conversationId = viewUserId, event = ChatConstant.MSG_ALERT
        )
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.REFRESH_USER_CACHE)
        EmMsgManager.sendCmdMessagePeople(
            viewUserId,
            "恭喜，你与${ImUserManager.getSelfUserInfo().nickName}已成为情侣！",
            ChatConstant.ACTION_BIND_LOVERS_SUCCESS
        )
    }

    private fun showCancelLoversPop(): BasePopupView {
        val nickName = if (loversInfo!!.viewInfo.userId == AppCacheManager.userId) {
            loversInfo!!.loverInfo!!.nickName
        } else {
            loversInfo!!.viewInfo.nickName
        }
        val content = Spans.builder()
            .text("确认要解除你和${nickName}的情侣关系吗？\n\n")
            .text("温馨提示：需支付${loversInfo?.cancalPrice}玫瑰")
            .color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.colorMain
                )
            ).build()

        return DialogUtils.showConfirmDialog(
            "解除情侣",
            {
                DialogUtils.showLoading()
                val cancelUserId = if (loversInfo!!.viewInfo.userId == AppCacheManager.userId) {
                    loversInfo!!.loverInfo!!.userId
                } else {
                    loversInfo!!.viewInfo.userId
                }
                mViewModel.cancelLovers(cancelUserId)
            },
            {
            },
            content = content,
            cancel = "取消",
            confirm = "确认",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }
}
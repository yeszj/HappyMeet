package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.lxj.xpopup.core.BottomPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.HeartCpSignInfo
import cn.huanyuan.sweetlove.bean.HeartCpSignResponse
import cn.huanyuan.sweetlove.bean.reqeust.HeartCpSignRequest
import cn.huanyuan.sweetlove.databinding.PopHeartCpSignBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.event.cp.CpSignItemAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.ui.chat.ImChatActivity
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2026/3/24
 * desc:
 */
@SuppressLint("ViewConstructor")
class HeartCpSignPop(
    val context: FragmentActivity,
    val activityId: String,
    var heartSingRes: HeartCpSignResponse,
    val onHeartCpClickListener: OnHeartCpClickListener
) : BottomPopupView(context) {
    private val mAdapter by lazy { CpSignItemAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_heart_cp_sign
    }

    companion object {
        const val STATUS_BIND_CP = 0

        const val STATUS_CLICK = 1
        const val STATUS_INVITE = 2
        const val STATUS_SEND_GIFT = 3
        const val STATUS_RECEIVE_REWARD = 4
        const val STATUS_MAKEUP_SIGN = 5
        const val STATUS_HAS_RECEIVE = 6

        @JvmStatic
        fun showDialog(
            context: FragmentActivity,
            activityId: String,
            heartSingRes: HeartCpSignResponse,
            onHeartCpClickListener: OnHeartCpClickListener, simpleCallback: SimpleCallback
        ): HeartCpSignPop {
            val matchPop =
                HeartCpSignPop(context, activityId, heartSingRes, onHeartCpClickListener)
            val builder = XPopup.Builder(context).setPopupCallback(simpleCallback)
            builder
                .maxHeight(com.blankj.utilcode.util.ScreenUtils.getScreenHeight())
                .asCustom(matchPop)
                .show()
            return matchPop
        }

    }

    private lateinit var mBinding: PopHeartCpSignBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopHeartCpSignBinding.bind(popupImplView)
        mBinding.rvSign.adapter = mAdapter


        bindData()

        initListener()

        refreshSignInfo()

    }

    @SuppressLint("SetTextI18n")
    private fun bindData() {
        GlideUtils.load(
            context,
            heartSingRes.myPortrait,
            mBinding.ivAvatarA
        )
        val hasBindCp = !CommonUtils.isEmpty(heartSingRes.cpPortrait)
        mAdapter.setHasBindCp(hasBindCp)
        if (!hasBindCp){
            mBinding.ivAvatarB.setImageResource(R.mipmap.icon_avatar_heart_default)
            mBinding.tvIntimacy.text = ""
        }else{
            GlideUtils.load(
                context,
                heartSingRes.cpPortrait,
                mBinding.ivAvatarB
            )
            mBinding.tvIntimacy.text = heartSingRes.intimacy
        }

        val days = heartSingRes.days
        mAdapter.submitList(days)
        if (hasBindCp) {
            mBinding.tvDays.text = "已连续点亮${heartSingRes.consecutiveDays}天｜连续越久奖励越丰厚"
            val selectSignItem = mAdapter.getSelectSignItem()
            if (selectSignItem == null) {
                setDefaultSelect(days)
            } else {
                switchSelectItem()
            }
        } else {
            mBinding.tvDays.text = "快去绑定你的心动CP吧"
            mBinding.tvValue.text = "绑定心动CP"
            mBinding.vgOperate.tag = STATUS_BIND_CP
            mBinding.tvTips.visibility = View.INVISIBLE
        }
    }

    private fun initListener() {
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
        mAdapter.addOnItemChildClickListener(R.id.viewBg) { _, view, position ->
            val item = mAdapter.getItem(position) ?: return@addOnItemChildClickListener
            if (CommonUtils.isEmpty(heartSingRes.cpPortrait)) {
                //未绑定cp不可点
                return@addOnItemChildClickListener
            }
            if (!item.afterBind) {
                //绑定cp之前的不可点
                return@addOnItemChildClickListener
            }
            if (DateUtils.compareWithToday(item.date) == 1) {
                //当天之后不可点
                return@addOnItemChildClickListener
            }
            mAdapter.setSelectPosition(position)
            switchSelectItem()
        }
        mBinding.vgOperate.setOnSingleClickListener {
            val tag = mBinding.vgOperate.tag
            if (tag != null && tag is Int) {
                when (tag) {
                    STATUS_BIND_CP -> {
                        onHeartCpClickListener.onBindCp()
                    }

                    STATUS_CLICK -> {
                        //立即点亮
                        signIn()
                    }

                    STATUS_MAKEUP_SIGN -> {
                        //补签
                        DialogUtils.showConfirmDialog("补签提醒", {
                            makeUpSignIn()
                        },{},"补签需消费20玫瑰，补签后状态将变为“双方点亮”，确认补签吗？","再想想","确认补签")
                    }

                    STATUS_INVITE -> {
                        sendInviteSignMsg()
                    }

                    STATUS_SEND_GIFT -> {
                        clickSendGift()
                    }

                    STATUS_RECEIVE_REWARD -> {
                        receiveReward()
                    }
                }
            }
        }
    }

    private fun receiveReward() {
        val selectSignItem = mAdapter.getSelectSignItem()
        val signRequest = HeartCpSignRequest(activityId, selectSignItem!!.date)
        request(
            { rxApi.receiveReward(signRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    refreshSignInfo()
                    showToast(data.data)
                }
            })
    }

    private fun clickSendGift() {
        val cpRoomId = heartSingRes.cpRoomId
        if (!CommonUtils.isEmpty(cpRoomId)) {
            LiveRoomManager.toLiveRoomPage(context, cpRoomId)
        } else {
            ImChatActivity.lunch(context, heartSingRes.cpUserId)
            showToast("对方未在直播，去提醒直播吧～")
        }
    }

    private fun sendInviteSignMsg() {
        val params = HashMap<String, String>()
        params[ImMessageParamsConfig.SENDSHOWCONTENT] =
            "已发送提醒消息 请等待您的心动CP点亮～"
        params[ImMessageParamsConfig.RECEIVESHOWCONTENT] =
            "您的心动CP正在等待您今日点亮哦～"
        params[ImMessageParamsConfig.KEY_BTN_VALUE] =
            "去点亮"
        params[ImMessageParamsConfig.KEY_PAGE_URL] =
            "{clsPath:cn.huanyuan.sweetlove.ui.event.cp.HeartCpEventActivity,activityId:${activityId}}"
        params[ImMessageParamsConfig.KEY_BTN_STATUS] = "2"
        EmMsgManager.sendCommonTipMsg(heartSingRes.cpUserId, params)
        showToast("已发送提醒消息 请等待您的心动CP点亮～")
    }

    private fun makeUpSignIn() {
        val selectSignItem = mAdapter.getSelectSignItem()
        val signRequest = HeartCpSignRequest(activityId, selectSignItem!!.date)
        request(
            { rxApi.makeUpSignIn(signRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("补签成功，可领取当日奖励")
                    refreshSignInfo()
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        ApplicationProxy.instance.showRechargePop(context, true, true)
                    }
                }
            })
    }

    private fun signIn() {
        val selectSignItem = mAdapter.getSelectSignItem()
        val signRequest = HeartCpSignRequest(activityId, selectSignItem!!.date)
        request(
            { rxApi.signInHeartCp(signRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    //自己签到成功
                    if (selectSignItem.signStatus==0){
                        //双方未签
                        showToast("今日点亮成功，等待对方点亮")
                    }else if (selectSignItem.signStatus==1){
                        //对方已签
                        showToast("今日点亮成功，可领取今日奖励")
                    }
                    refreshSignInfo()
                }
            })
    }

    private fun refreshSignInfo() {
        request(
            { rxApi.getSignInCalendar(activityId) },
            object : OnRequestResultListener<HeartCpSignResponse> {
                override fun onSuccess(data: BaseBean<HeartCpSignResponse>) {
                    heartSingRes = data.data ?: return
                    bindData()
                }
            })
    }

    private fun setDefaultSelect(days: MutableList<HeartCpSignInfo>) {
        val todayStr = DateUtils.getTodayStr()
        for (i in 0 until days.size) {
            val day = days[i]
            if (day.date == todayStr) {
                mAdapter.setSelectPosition(i)
                switchSelectItem()
                break
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun switchSelectItem() {
        val selectSignItem = mAdapter.getSelectSignItem()
        mBinding.tvTips.visibility = INVISIBLE
        if (selectSignItem != null) {
            val signStatus = selectSignItem.signStatus
            val rewardStatus = selectSignItem.rewardStatus

            val result = DateUtils.compareWithToday(selectSignItem.date)
            if (result == -1) {
                if (signStatus != 3) {
                    //显示补签
                    mBinding.tvValue.text = "立即补签"
                    mBinding.vgOperate.tag = STATUS_MAKEUP_SIGN
                } else {
                    showReceiveStyle(rewardStatus, selectSignItem)
                }
            } else if (result == 0) {
                if (signStatus == 0 || signStatus == 1) {
                    mBinding.tvValue.text = "立即点亮"
                    mBinding.vgOperate.tag = STATUS_CLICK
                } else if (signStatus == 2) {
                    mBinding.tvValue.text = "邀请点亮"
                    mBinding.vgOperate.tag = STATUS_INVITE
                } else {
                    showReceiveStyle(rewardStatus, selectSignItem)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showReceiveStyle(
        rewardStatus: Int,
        selectSignItem: HeartCpSignInfo
    ) {
        if (rewardStatus == 2) {
            mBinding.tvValue.text = "已领取"
            mBinding.vgOperate.tag = STATUS_HAS_RECEIVE
        } else {
            val intimacyDiff = selectSignItem.intimacyDiff
            if (CommonUtils.compareZero(intimacyDiff)) {
                mBinding.tvValue.text = "送礼增加亲密值"
                mBinding.tvTips.text = "还差${intimacyDiff}亲密值可领取今日奖励"
                mBinding.tvTips.visibility = VISIBLE
                mBinding.vgOperate.tag = STATUS_SEND_GIFT

            } else {
                mBinding.tvValue.text = "领取奖励"
                mBinding.vgOperate.tag = STATUS_RECEIVE_REWARD

            }
        }
    }

    interface OnHeartCpClickListener {
        fun onBindCp()
    }
}
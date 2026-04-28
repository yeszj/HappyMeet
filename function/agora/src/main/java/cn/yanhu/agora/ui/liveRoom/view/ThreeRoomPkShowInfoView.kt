package cn.yanhu.agora.ui.liveRoom.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.PkAgreeResponse
import cn.yanhu.agora.bean.PkContinueInfo
import cn.yanhu.agora.databinding.ViewPkShowInfoBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.PkRoomResultInfo
import cn.yanhu.commonres.bean.RoomPkEnterInfo
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2024/9/10
 * desc:
 */
class ThreeRoomPkShowInfoView : LinearLayout {
    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        initView(context, attrs)
    }

    private lateinit var mBinding: ViewPkShowInfoBinding



    @SuppressLint("Recycle")
    private fun initView(context: Context, attrs: AttributeSet?) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_pk_show_info, this, true
        )
        mBinding.tvDisConnect.setOnSingleClickListener {
            breakPk()
        }
    }

    var isRoomOwner: Boolean = false

    var pkInfo: PkAgreeResponse? = null
    private var pkOwnerUserId: String = ""
    private var pkTime: Int = 0
    fun startPK(
        pkAgreeResponse: PkAgreeResponse,
        myRoomOwnerAvatar: String,
        sendStartMsg: Boolean = true
    ) {
        pkInfo = pkAgreeResponse
        pkTime = pkInfo!!.pkTime
        CoilImgUtils.loadCircleImg(
            myRoomOwnerAvatar, mBinding.ivMyAvatar, borderWidth = CommonUtils.getDimension(
                com.zj.dimens.R.dimen.dp_1
            ).toFloat()
        )
        val seatList = pkAgreeResponse.seatList
        if (seatList.isNotEmpty()) {
            val roomUserSeatInfo = seatList[0].roomUserSeatInfo
            val portrait = roomUserSeatInfo?.portrait
            pkOwnerUserId = roomUserSeatInfo?.userId.toString()
            CoilImgUtils.loadCircleImg(
                portrait, mBinding.ivOtherAvatar, borderWidth = CommonUtils.getDimension(
                    com.zj.dimens.R.dimen.dp_1
                ).toFloat()
            )
        } else if (!TextUtils.isEmpty(pkAgreeResponse.otherRoomPortrait)) {
            CoilImgUtils.loadCircleImg(
                pkAgreeResponse.otherRoomPortrait,
                mBinding.ivOtherAvatar,
                borderWidth = CommonUtils.getDimension(
                    com.zj.dimens.R.dimen.dp_1
                ).toFloat()
            )
        }
        reStartPk(sendStartMsg)
    }

    /**
     * 重新开始PK
     */
    @SuppressLint("SetTextI18n")
    fun reStartPk(sendStartMsg: Boolean = true) {
        if (sendStartMsg) {
            pkInfo?.countDownTime = 0
        }
        mBinding.lottiePoint.playAnimation()
        mBinding.ivOtherResult.visibility = INVISIBLE
        mBinding.ivMyResult.visibility = INVISIBLE
        mBinding.ivPk.visibility = VISIBLE
        mBinding.ivStartTag.playAnimation()
        mBinding.vgTime1.visibility = VISIBLE
        mBinding.vgContinueTime.visibility = INVISIBLE
        if (sendStartMsg) {
            mBinding.tvMyCount.text = "0"
            mBinding.tvOtherCount.text = "0"
            ViewUtils.setViewWidth(mBinding.ivRedProgress, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142))
        }
        startPkCountTime()
        if (isRoomOwner && sendStartMsg) {
            onPkOperateListener?.onStartPk()
        }
    }

    fun continuePk(pkAgreeResponse: PkContinueInfo) {
        pkTimer?.cancel()
        pkInfo?.pkId = pkAgreeResponse.pkId
        if (isRoomOwner) {
            startWaitAgreeCountdown()
        } else {
            mBinding.vgTime1.visibility = VISIBLE
            mBinding.vgContinueTime.visibility = INVISIBLE
            mBinding.ivPk.visibility = GONE
            mBinding.tvCountTime.text = "主持连线中"
        }
    }

    private var pkTimer: CountDownTimer? = null

    /**
     * pk开始倒计时
     */
    private fun startPkCountTime() {
        val countDownTime = pkInfo!!.countDownTime
        val time = if (countDownTime > 0) {
            countDownTime
        } else {
            pkInfo!!.pkTime
        }
        pkInfo?.countDownTime = 0
        pkTimer?.cancel()
        pkTimer = object : CountDownTimer((1000 * time).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val stringForTime2 = DateUtils.stringForTime2((millisUntilFinished/1000).toInt())
                mBinding.tvCountTime.text = stringForTime2
            }

            override fun onFinish() {
                //pk已结束 显示pk结果
                if (isRoomOwner) {
                    finishPk()
                }
            }
        }
        pkTimer!!.start()
    }

    private fun onCurrentPkFinish(countTime: Int) {
        if (isRoomOwner) {
            mBinding.vgTime1.visibility = INVISIBLE
            mBinding.vgContinueTime.visibility = VISIBLE
          //  startContinueCountdown(countTime)
            mBinding.tvNextCountDown.text =
                "继续PK"
            mBinding.tvNextCountDown.setOnSingleClickListener {
                if (isRoomOwner) {
                    clickContinue()
                }
            }
        } else {
            mBinding.vgContinueTime.visibility = INVISIBLE
            mBinding.ivPk.visibility = GONE
            mBinding.tvCountTime.text = "PK结束"

           // startEndCountdown(countTime)
        }
    }

    @SuppressLint("CheckResult")
    private fun clickContinue() {
        request({ agoraRxApi.continuePk(pkInfo!!.roomId, pkInfo!!.pkRoomId, pkInfo!!.pkTime) },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {

            }
        })
    }

    /**
     * 房间成员倒计时
     * pk结束
     */
    private fun startEndCountdown(countTime: Int) {
        pkTimer?.cancel()
        mBinding.tvCountTime.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        pkTimer = object : CountDownTimer((1000 * countTime).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvCountTime.text = "PK结束(" + (millisUntilFinished / 1000).toInt() + "s)"
            }

            override fun onFinish() {

            }
        }
        pkTimer!!.start()
    }

    /**房主倒计时
     * pk结束 是否继续倒计时 1分钟
     * 倒计时结束后 回到原直播间样式
     */
    private fun startContinueCountdown(countTime:Int = 60) {
        pkTimer?.cancel()
        mBinding.tvNextCountDown.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        pkTimer = object : CountDownTimer((1000 * countTime).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvNextCountDown.text =
                    "继续PK(" + (millisUntilFinished / 1000).toInt() + "s)"
            }

            override fun onFinish() {
                //断开连线 回到原直播间样式
                breakPk()
            }
        }
        pkTimer?.start()
    }

    /**
     * 点击继续pK 等待对方同意 倒计时15s
     * 倒计时结束 回到原直播间
     */
    private fun startWaitAgreeCountdown() {
        pkTimer?.cancel()
        mBinding.tvNextCountDown.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.whiteColorAlpha50))
        pkTimer = object : CountDownTimer((1000 * 15).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvNextCountDown.text =
                    "已邀请(" + (millisUntilFinished / 1000).toInt() + ")"
            }

            override fun onFinish() {
                //pk已结束 回到原直播间样式
                breakPk()
            }
        }
        pkTimer?.start()
    }

    /**
     * 本轮PK结束
     */
    @SuppressLint("CheckResult")
    private fun finishPk() {
        request({agoraRxApi.finishPk(pkInfo?.pkId)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {

            }

        })
    }

    /**
     * 断开连接
     */
    @SuppressLint("CheckResult")
    fun breakPk() {
        AgoraManager.getInstance().leavePkChannel()
        request({agoraRxApi.breakPk(pkInfo!!.pkId)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
            }

        })
    }

    /**
     * 同意继续PK
     */
    @SuppressLint("CheckResult")
    fun continuePkAgree() {
        request({agoraRxApi.continuePkAgree(pkInfo!!.roomId, pkInfo!!.pkId)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
            }
        })
    }

    fun cancel() {
        pkTimer?.cancel()
    }

    fun showPkInfo(pkEnterInfo: RoomPkEnterInfo) {

        CoilImgUtils.loadCircleImg( pkEnterInfo.otherRoomPortrait, mBinding.ivOtherAvatar, borderWidth = CommonUtils.getDimension(
            com.zj.dimens.R.dimen.dp_1).toFloat())
        CoilImgUtils.loadCircleImg( pkEnterInfo.nowRoomPortrait, mBinding.ivMyAvatar,borderWidth = CommonUtils.getDimension(
            com.zj.dimens.R.dimen.dp_1).toFloat())
        val result = PkRoomResultInfo(pkEnterInfo.roomPkValue, pkEnterInfo.otherRoomPkValue, 0)
        setPkProgressValue(result, true)
    }

    fun showResult(agreeResponse: PkRoomResultInfo) {
        if (agreeResponse.countDownTime == 0) {
            agreeResponse.countDownTime = 60
        }
        val roomPkValue = agreeResponse.roomPkValue
        val otherRoomPkValue = agreeResponse.otherRoomPkValue
        mBinding.ivMyResult.visibility = VISIBLE
        mBinding.ivOtherResult.visibility = VISIBLE
        mBinding.tvMyCount.text = roomPkValue.toString()
        mBinding.tvOtherCount.text = otherRoomPkValue.toString()
        if (roomPkValue > otherRoomPkValue) {
            mBinding.ivMyResult.setImageResource(R.drawable.pk_success)
            mBinding.ivOtherResult.setImageResource(R.drawable.pk_fail)
        } else if (roomPkValue < otherRoomPkValue) {
            mBinding.ivMyResult.setImageResource(R.drawable.pk_fail)
            mBinding.ivOtherResult.setImageResource(R.drawable.pk_success)
        } else {
            mBinding.ivMyResult.setImageResource(R.drawable.pk_draw)
            mBinding.ivOtherResult.setImageResource(R.drawable.pk_draw)
        }
        setPkProgressValue(agreeResponse, true)
        onCurrentPkFinish(agreeResponse.countDownTime)
    }

    fun setPkProgressValue(agreeResponse: PkRoomResultInfo, isFinish: Boolean) {
        val roomPkValue = agreeResponse.roomPkValue
        val otherRoomPkValue = agreeResponse.otherRoomPkValue
        val totalProgress = roomPkValue + otherRoomPkValue
        if (totalProgress > 0) {
            val toInt = mBinding.tvMyCount.text.toString().toInt()
            if (isFinish) {
                mBinding.tvMyCount.text = roomPkValue.toString()
            } else {
                if (roomPkValue >= toInt) {
                    mBinding.tvMyCount.text = roomPkValue.toString()
                }
            }
            mBinding.tvOtherCount.text = otherRoomPkValue.toString()
            if (roomPkValue == otherRoomPkValue) {
                ViewUtils.setViewWidth(
                    mBinding.ivRedProgress,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_132)
                )
            } else {
                val totalWidth = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_290)
                var width = roomPkValue * totalWidth / totalProgress
                if (width > totalWidth) {
                    width = totalWidth
                }
                ViewUtils.setViewWidth(mBinding.ivRedProgress, width)
            }
        }
//        if (roomPkValue >= 4000 && isRoomOwner && checkFinish) {
//            finishPk()
//        }
    }

    @SuppressLint("CheckResult")
    fun operatePkMikeUse(mikeUse: Boolean) {
        request({agoraRxApi.operatePkMikeUse(pkInfo!!.pkRoomId, mikeUse)},object : OnRequestResultListener<Boolean>{
            override fun onSuccess(data: BaseBean<Boolean>) {
            }
        })
    }

    var onPkOperateListener: OnPkOperateListener? = null

    interface OnPkOperateListener {
        fun onStartPk()
    }

}
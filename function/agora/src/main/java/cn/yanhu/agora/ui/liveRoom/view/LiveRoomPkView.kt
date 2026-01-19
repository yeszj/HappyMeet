package cn.yanhu.agora.ui.liveRoom.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.commonres.bean.RoomPkInfo
import cn.yanhu.agora.databinding.ViewLiveRoomPkBinding
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import androidx.core.view.isGone
import androidx.core.view.isVisible
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener

/**
 * @author: zhengjun
 * created: 2025/9/3
 * desc:
 */
class LiveRoomPkView : LinearLayout {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private lateinit var mBinding: ViewLiveRoomPkBinding
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_live_room_pk, this, true
        )
    }


    @SuppressLint("SetTextI18n")
    fun setPkInfo(roomPkInfo: RoomPkInfo?, roomDetailInfo: RoomDetailInfo) {
        if (roomPkInfo==null || roomPkInfo.status == RoomPkInfo.STATUS_END){
            countDown?.cancel()
            hidePkView()
            return
        }
        mBinding.vgTime.setOnSingleClickListener {
            if (roomDetailInfo.isOwner() && roomPkInfo.status == RoomPkInfo.STATUS_RESULT){
                LiveEventBus.get<RoomPkInfo>(EventBusKeyConfig.CONTINUEPK).post(roomPkInfo)
            }
        }
        val status = roomPkInfo.status
        mBinding.redAvatar.setAvatarUrls(roomPkInfo.redMemberSeatList, isRedUser = true)
        mBinding.blueAvatar.setAvatarUrls(roomPkInfo.blueMemberSeatList, isRedUser = false)
        val redCnt = roomPkInfo.redCnt
        val blueCnt = roomPkInfo.blueCnt
        mBinding.tvRedValue.text = redCnt.toString()
        mBinding.tvBlueValue.text = blueCnt.toString()
        val blueMemberList = roomPkInfo.blueMemberList
        if (redCnt == blueCnt){
            mBinding.pkProgress.max = 100
            mBinding.pkProgress.setProgress(50,true)
        }else{
            mBinding.pkProgress.max = redCnt+blueCnt
            mBinding.pkProgress.setProgress(redCnt,true)
        }
        if (blueMemberList!=null && blueMemberList.contains(AppCacheManager.userId)){
            mBinding.pkProgress.progressDrawable = ContextCompat.getDrawable(context,R.drawable.pk_progress_blue_bg)
        }else{
            mBinding.pkProgress.progressDrawable = ContextCompat.getDrawable(context,R.drawable.pk_progress_red_bg)
        }

        when (status) {
            RoomPkInfo.STATUS_PLAYING -> {
                mBinding.ivRedResult.visibility = INVISIBLE
                mBinding.ivBlueResult.visibility = INVISIBLE
                mBinding.ivTxtPk.visibility = VISIBLE
                ViewUtils.setViewWidth(mBinding.vgTime, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_98))
                mBinding.tvTime.text = DateUtils.stringForTime2(roomPkInfo.countDown)
                startCountDown(roomPkInfo, roomDetailInfo)
                if (roomPkInfo.showPkStart=="0"){
                    mBinding.ivStartTag.playAnimation()
                }
                showPkView()
            }

            RoomPkInfo.STATUS_RESULT -> {
                mBinding.ivRedResult.visibility = VISIBLE
                mBinding.ivBlueResult.visibility = VISIBLE
                if (roomPkInfo.result==RoomPkInfo.RESULT_RED_SUCCESS){
                    mBinding.ivRedResult.setImageResource(R.drawable.pk_success)
                    mBinding.ivBlueResult.setImageResource(R.drawable.pk_fail)
                }else if (roomPkInfo.result == RoomPkInfo.RESULT_BLUE_SUCCESS){
                    mBinding.ivRedResult.setImageResource(R.drawable.pk_fail)
                    mBinding.ivBlueResult.setImageResource(R.drawable.pk_success)
                }else{
                    mBinding.ivRedResult.setImageResource(R.drawable.pk_draw)
                    mBinding.ivBlueResult.setImageResource(R.drawable.pk_draw)
                }
                ViewUtils.setViewWidth(mBinding.vgTime, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_113))
                mBinding.ivTxtPk.visibility = GONE
                showPkView()
                val isOwner = roomDetailInfo.isOwner()
                setResultTime(isOwner, roomPkInfo.countDown)
                startCountDown(roomPkInfo, roomDetailInfo)
            }
        }
    }

    private fun showPkView() {
        if (this.isGone) {
            this.visibility = VISIBLE
            LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_MSG_TOP).post(true)
        }
    }

    private fun hidePkView() {
        if (this.isVisible) {
            this.visibility = GONE
            mBinding.redAvatar.clearAvatars()
            mBinding.blueAvatar.clearAvatars()
            LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_MSG_TOP).post(false)
        }
    }


    private var countDown: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun startCountDown(roomPkInfo: RoomPkInfo, roomDetailInfo: RoomDetailInfo) {
        val isOwner = roomDetailInfo.isOwner()
        countDown?.cancel()
        (context as FragmentActivity).countDown(roomPkInfo.countDown, start = {
            countDown = it
        }, end = {
            //倒计时结束
            countDown = null
            if (isOwner && roomPkInfo.status == RoomPkInfo.STATUS_PLAYING){
                endPk(roomPkInfo,roomDetailInfo)
                return@countDown
            }
            if (roomPkInfo.status == RoomPkInfo.STATUS_RESULT){
                hidePkView()
            }
        }, next = {
            if (roomPkInfo.status == RoomPkInfo.STATUS_PLAYING) {
                mBinding.tvTime.text = DateUtils.stringForTime2(it)
            } else {
                setResultTime(isOwner, it)
            }
        }, cancel = {})
    }

    private fun endPk(roomPkInfo: RoomPkInfo,roomDetailInfo: RoomDetailInfo) {
        request({ agoraRxApi.endPk(roomId = roomDetailInfo.roomId,roomPkInfo.pkId) },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
            }
        }, isShowToast = false)
    }

    @SuppressLint("SetTextI18n")
    private fun setResultTime(isOwner: Boolean, second: Int) {
        if (isOwner) {
            mBinding.tvTime.text = "继续PK(${second}s)"
        } else {
            mBinding.tvTime.text = "PK结束(${second}s)"
        }
    }

    fun onDestroy() {
        countDown?.cancel()
    }
}
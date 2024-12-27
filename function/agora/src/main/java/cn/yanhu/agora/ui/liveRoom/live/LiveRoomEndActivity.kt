package cn.yanhu.agora.ui.liveRoom.live

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.agora.databinding.ActivityLiveRoomEndBinding
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.google.gson.Gson

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
class LiveRoomEndActivity : BaseActivity<ActivityLiveRoomEndBinding, LiveRoomViewModel>(
    R.layout.activity_live_room_end,
    LiveRoomViewModel::class.java
) {
    @SuppressLint("SetTextI18n")
    override fun initData() {
        setFullScreenStatusBar(false)
        val upActivity: Activity? = MiniWindowManager.getUpActivity()
        (upActivity as? LiveRoomEndActivity)?.finish()
        val extras = intent.extras
        LiveDataEventManager.sendLiveDataMessage(
            EventBusKeyConfig.CLOSELIVEROOM,
            ""
        )
        val type = extras!!.getInt("type", LiveRoomManager.HOUSE_OFF)

        if (type == LiveRoomManager.HOUSE_OWNER_OFF) {
            val data: RoomLeaveResponse? = Gson().fromJson(
                extras.getString("data"),
                RoomLeaveResponse::class.java
            )
            if (data != null) {
                mBinding.info = data
            }
        } else if (type == LiveRoomManager.HOUSE_CUT_EXTRA_KICK) {
            mBinding.liveRoomEndTxtTwo.text = "直播已结束"
        } else if (type == LiveRoomManager.HOUSE_NOT_FUNDS) {
            mBinding.liveRoomEndTxtTwo.text = "专属房间所需玫瑰余额不足\n" + "私密约会已结束"
        } else if (type == LiveRoomManager.HOUSE_ADMINISTRATOR_OFF) {
            val roomId = extras.getString(IntentKeyConfig.ROOM_ID)
            mBinding.liveRoomEndTxtTwo.text = "房间已被管理员强制关闭"
            roomId?.apply {
                bindCloseReason(roomId)
            }
        }
        mBinding.liveRoomEndOff.setOnClickListener { finish() }
    }

    @SuppressLint("CheckResult")
    private fun bindCloseReason(roomId: String) {
        request({ agoraRxApi.getRoomCloseReason(roomId)},object : OnRequestResultListener<String?>{
            @SuppressLint("SetTextI18n")
            override fun onSuccess(data: BaseBean<String?>) {
                val reason: String? = data.data
                if (!TextUtils.isEmpty(reason)) {
                    mBinding.tvReason.visibility = View.VISIBLE
                    mBinding.tvReason.text = "关房原因：$reason"
                }
            }

        })
    }
    companion object{
        fun lunch(context: Context,type:Int,resultInfo:String, roomId:String){
            //管理员强制关闭
            val intent = Intent(context, LiveRoomEndActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra(
                IntentKeyConfig.ROOM_ID,
                roomId)
            intent.putExtra("data",resultInfo)
            context.startActivity(intent)
        }
    }
}
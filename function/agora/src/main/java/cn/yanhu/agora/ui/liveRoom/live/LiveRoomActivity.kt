package cn.yanhu.agora.ui.liveRoom.live

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.core.content.ContentProviderCompat.requireContext
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityLiveRoomBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.manager.LiveWebSocketManager
import cn.yanhu.agora.manager.monitor.ComprehensiveFrameRateMonitor
import cn.yanhu.agora.manager.monitor.MemoryMonitor
import cn.yanhu.agora.manager.refreshRate.SmartRefreshRateManager
import cn.yanhu.agora.service.LocalRecordingService
import cn.yanhu.agora.service.LocalServiceManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.agora.ui.liveRoom.live.SlideLiveRoomActivity.Companion.LIVE_ROOM_TAG
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.OnRoomLeaveListener
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.BuildConfig
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
@Route(path = RouterPath.ROUTER_LIVE_ROOM)
class LiveRoomActivity : BaseActivity<ActivityLiveRoomBinding, LiveRoomViewModel>(
    R.layout.activity_live_room, LiveRoomViewModel::class.java
) {

    private var liveRoomFrg: BaseLiveRoomFrg? = null
    override fun initData() {
        val roomSourceBean: RoomDetailInfo? =
            intent.getSerializableExtra(IntentKeyConfig.DATA) as RoomDetailInfo?
        if (roomSourceBean == null) {
            finish()
            return
        }
        if (roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
            liveRoomFrg = ThreeLiveRoomFrg()
            liveRoomFrg?.arguments = intent.extras
            addFragment(liveRoomFrg)
        } else if (roomSourceBean.getFragmentType() == RoomListBean.FRG_SONG_ROOM
            || roomSourceBean.getFragmentType() == RoomListBean.FRG_NINE_ROOM
            || roomSourceBean.getFragmentType() == RoomListBean.FRG_SEVEN_ROOM
        ) {
            liveRoomFrg = MoreSeatLiveRoomFrg()
            liveRoomFrg?.arguments = intent.extras
            addFragment(liveRoomFrg)
        } else {
            val upgradeTipFrg = NeedUpgradeTipFrg()
            addFragment(upgradeTipFrg)
        }
        setFullScreenStatusBar(true)
        AgoraManager.callType = 1
        AgoraManager.isLiveRoom = true
        checkMemory()
    }

    private var checkMemoryScope:CoroutineScope?=null
    private fun checkMemory() {
        mContext.countDown(60 * 20 * 8, 60000 * 5, start = {
            checkMemoryScope = it
        }, end = {
            //倒计时结束
        }, next = {
            MemoryMonitor.logMemorySnapshot(mContext)
        }, cancel = {})
    }


    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        liveRoomFrg?.onNewIntent(intent)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (liveRoomFrg != null) {
            liveRoomFrg?.showFloatWindow(1)
        } else {
            super.onBackPressed()
        }
    }

    override fun registerNecessaryObserver() {
        //super.registerNecessaryObserver()
    }

    override fun onResume() {
        super.onResume()
        if (!LocalRecordingService.isRunning) {
            logcom("开启通话前台服务")
            LocalServiceManager.startLocalService(mContext)
        }
    }

    override fun onStop() {
        super.onStop()
        liveRoomFrg?.onCustomStop()
    }

    override fun exactDestroy() {
        super.exactDestroy()
        LiveWebSocketManager.getInstance().destroy()
        AgoraManager.isLiveRoom = false
        checkMemoryScope?.cancel()
        if (LocalRecordingService.isRunning) {
            LocalServiceManager.stopLocalService(mContext)
        }
        if (liveRoomFrg?.isFinish == true) {
            logComToFile(LIVE_ROOM_TAG, "用户主动退出")
        } else {
            logComToFile(LIVE_ROOM_TAG, "系统回收销毁")
        }
        liveRoomFrg?.exitRoom()
    }


    fun roomLeave(onRoomLeaveListener: OnRoomLeaveListener) {
        logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "进入其它房间，手动退出当前房间")
        liveRoomFrg?.roomLeave(onRoomLeaveListener)
    }


    companion object {
        const val LIVE_ROOM_TAG = "liveRoom"
    }
}
package cn.yanhu.agora.ui.liveRoom.live

import android.annotation.SuppressLint
import android.content.Intent
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityLiveRoomBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.zj.netrequest.OnRoomLeaveListener
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouterPath
import com.alibaba.android.arouter.facade.annotation.Route

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
        if (roomSourceBean.roomType == RoomListBean.TYPE_THREE_ROOM) {
            liveRoomFrg = ThreeLiveRoomFrg()
            liveRoomFrg?.arguments = intent.extras
            addFragment(liveRoomFrg)
        } else {
            val upgradeTipFrg = NeedUpgradeTipFrg()
            addFragment(upgradeTipFrg)
        }
        setFullScreenStatusBar(true)
        AgoraManager.isLiveRoom = true
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

    override fun onStop() {
        super.onStop()
        liveRoomFrg?.onCustomStop()
    }

    override fun exactDestroy() {
        super.exactDestroy()
        liveRoomFrg?.exitRoom()
        AgoraManager.isLiveRoom = false
    }

    fun roomLeave(onRoomLeaveListener: OnRoomLeaveListener){
        liveRoomFrg?.roomLeave(onRoomLeaveListener)
    }


    companion object {
        const val LIVE_ROOM_TAG = "liveRoom"
    }
}
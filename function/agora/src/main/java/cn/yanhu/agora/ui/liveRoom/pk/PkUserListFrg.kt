package cn.yanhu.agora.ui.liveRoom.pk

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.PkUserListAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.PkInfoResponse
import cn.yanhu.agora.databinding.FrgPkUserListBinding
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlin.getValue
import kotlin.jvm.java

/**
 * @author: zhengjun
 * created: 2024/9/9
 * desc:
 */
class PkUserListFrg : BaseFragment<FrgPkUserListBinding, LiveRoomViewModel>(R.layout.frg_pk_user_list,
    LiveRoomViewModel::class.java
) {
    private val pkUserListAdapter by lazy { PkUserListAdapter() }
    private var type: Int = TYPE_RECOMMEND
    private var currentRoomId:String = ""

    @SuppressLint("SetTextI18n")
    override fun initData() {
        currentRoomId = requireArguments().getString(IntentKeyConfig.ROOM_ID).toString()
        mBinding.recyclerView.adapter = pkUserListAdapter
        pkUserListAdapter.isStateViewEnable = true
        val emptyPkView = LayoutInflater.from(context).inflate(R.layout.view_empty_pk, null)
        pkUserListAdapter.stateView = emptyPkView
        pkUserListAdapter.addOnItemChildClickListener(
            R.id.tv_invite
        ) { _, _, position ->
            val item = pkUserListAdapter.getItem(position) ?: return@addOnItemChildClickListener
            val roomId = item.roomId
            if (item.inviteStatus == 0) {
                onInviteListener?.onInvite(roomId)
            } else {
                //取消邀请
                cancelInvite()
            }
        }
        type = requireArguments().getInt(IntentKeyConfig.TYPE)
        pkUserListAdapter.setIsRecently(type == TYPE_RECENT)
        val tvTips = emptyPkView.findViewById<TextView>(R.id.tv_tips)
        if (type == TYPE_RECOMMEND) {
            tvTips.text = "暂无推荐用户"
        } else {
            tvTips.text = "暂无近3个月PK的用户在线"
        }
    }
    private fun cancelInvite() {
        request({ agoraRxApi.cancelPkInvite(currentRoomId) },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_PK_INFO).post(true)
            }
        })
    }


    private var pkInfoRes: PkInfoResponse? = null
    fun setPkInfo(pkInfo: PkInfoResponse) {
        pkInfoRes = pkInfo
        val list = if (type == TYPE_RECOMMEND) {
            pkInfo.recommendPkRooms
        } else {
            pkInfo.recentlyPkRooms
        }
        pkUserListAdapter.isStateViewEnable = list.isEmpty()
        pkUserListAdapter.submitList(list)
    }


    override fun initRefresh() {
        super.initRefresh()
    }



    private var onInviteListener: OnInviteListener? = null

    fun setOnInviteListener(listener: OnInviteListener) {
        onInviteListener = listener
    }

    fun setInviteSuccess(targetRoomId: String) {
        val position = pkUserListAdapter.items.indexOfFirst {
            it.roomId == targetRoomId
        }
        if (position >= 0) {
            pkUserListAdapter.getItem(position)?.inviteStatus = 1
            pkUserListAdapter.notifyItemChanged(position)
        }
    }

    interface OnInviteListener {
        fun onInvite(roomId: String)
    }

    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_RECENT = 2
        fun newInstance(type: Int,roomId:String): PkUserListFrg {
            val args = Bundle()
            args.putInt(IntentKeyConfig.TYPE, type)
            args.putString(IntentKeyConfig.ROOM_ID,roomId)
            val fragment = PkUserListFrg()
            fragment.arguments = args
            return fragment
        }
    }
}
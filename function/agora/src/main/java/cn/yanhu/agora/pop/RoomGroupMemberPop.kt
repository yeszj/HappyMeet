package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.RoomGroupMemberAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomGroupMemberRes
import cn.yanhu.agora.databinding.PopLiveRoomGroupMemberBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class RoomGroupMemberPop(context: Context, private val rankList: RoomGroupMemberRes,var exitListener: OnExitGroupListener) :
    BottomPopupView(context) {
    private val userAdapter by lazy { RoomGroupMemberAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_group_member
    }

    private lateinit var mBiding: PopLiveRoomGroupMemberBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomGroupMemberBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无粉丝")
        mBiding.tvNickName.text = "${rankList.nickName}的粉丝团(${rankList.count}人)"
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        userAdapter.submitList(rankList.list)
        userAdapter.isStateViewEnable = rankList.list.isEmpty()
        userAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<UserDetailInfo>{
            override fun onClick(
                adapter: BaseQuickAdapter<UserDetailInfo, *>,
                view: View,
                position: Int
            ) {
                val item = userAdapter.getItem(position) ?: return
                RouteIntent.lunchPersonHomePage(item.userId)
            }
        })
        userAdapter.addOnItemChildClickListener(R.id.tv_exit) { _, view, position ->
            exitListener.onExitGroup()
        }
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
    }

    interface OnExitGroupListener {
        fun onExitGroup()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, rankList: RoomGroupMemberRes,exitListener: OnExitGroupListener
        ): RoomGroupMemberPop {
            val matchPop = RoomGroupMemberPop(mContext, rankList,exitListener)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
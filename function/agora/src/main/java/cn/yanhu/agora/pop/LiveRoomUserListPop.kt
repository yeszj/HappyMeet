package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomUserListAdapter
import cn.yanhu.agora.databinding.PopLiveRoomUserListBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.manager.EmMsgManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class LiveRoomUserListPop(
    context: Context,
    private val userList: MutableList<UserDetailInfo>,
    val roomDetailInfo: RoomDetailInfo
) : BottomPopupView(context) {
    private val userAdapter by lazy { LiveRoomUserListAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_user_list
    }

    private lateinit var mBiding: PopLiveRoomUserListBinding
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomUserListBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无在线用户")
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        userAdapter.submitList(userList)
        userAdapter.isStateViewEnable = userList.size <= 0
        userAdapter.addOnItemChildClickListener(R.id.tv_invite,
            object : BaseQuickAdapter.OnItemChildClickListener<UserDetailInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<UserDetailInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val ownerInfo = roomDetailInfo.ownerInfo?:return
                    val map: MutableMap<String, Any> = HashMap()
                    map["fromNickName"] = ownerInfo.nickName
                    map["portrait"] = ownerInfo.portrait
                    val item = userAdapter.getItem(position) ?: return
                    if (item.seatNum > 0) {
                        EmMsgManager.sendAgreeSeatApplyMsg(
                            item.userId,
                            item.nickName,
                            roomDetailInfo.roomId.toString(),
                            item.seatNum.toString()
                        )
                    } else {
                        EmMsgManager.sendCmdMessagePeople(
                            item.userId,
                            ChatConstant.ACTION_MSG_SET_UP,
                            map
                        )
                        showToast("上麦邀请已发送")
                    }
                }

            })
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userList: MutableList<UserDetailInfo>,
            ownerInfo: RoomDetailInfo,
        ): LiveRoomUserListPop {
            val matchPop = LiveRoomUserListPop(mContext, userList, ownerInfo)
            val builder =
                XPopup.Builder(mContext)
            builder.enableDrag(false).asCustom(matchPop).show()
            return matchPop
        }
    }
}
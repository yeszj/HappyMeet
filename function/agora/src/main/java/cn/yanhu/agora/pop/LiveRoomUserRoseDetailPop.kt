package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomUserRoseAdapter
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.databinding.PopLiveRoomUserRoseDetailBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.router.RouteIntent
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class LiveRoomUserRoseDetailPop(context: Context, private val roseInfo: UserReceiveRoseInfo) :
    BottomPopupView(context) {
    private val userAdapter by lazy { LiveRoomUserRoseAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_user_rose_detail
    }

    private lateinit var mBiding: PopLiveRoomUserRoseDetailBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomUserRoseDetailBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂未收到玫瑰")
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        val list = roseInfo.list
        userAdapter.submitList(list)
        mBiding.tvTotalRoseNum.text = "本场玫瑰：${roseInfo.roseNum}"
        mBiding.tvNickName.text = "${roseInfo.nickName}收到的玫瑰明细"
        userAdapter.isStateViewEnable = list.size <= 0
        userAdapter.addOnItemChildClickListener(R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildClickListener<UserDetailInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<UserDetailInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = userAdapter.getItem(position) ?: return
                    RouteIntent.lunchPersonHomePage(item.userId)
                }
            })
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, roseInfo: UserReceiveRoseInfo
        ): LiveRoomUserRoseDetailPop {
            val matchPop = LiveRoomUserRoseDetailPop(mContext, roseInfo)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
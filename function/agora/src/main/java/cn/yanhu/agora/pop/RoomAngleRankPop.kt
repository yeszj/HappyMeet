package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.AngleRankAdapter
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.databinding.PopLiveRoomUserRoseDetailBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
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
class RoomAngleRankPop(context: Context, private val rankList: List<AngleRankInfo>,val type:Int) :
    BottomPopupView(context) {
    private val userAdapter by lazy { AngleRankAdapter() }
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
        emptyView.setFootText("暂无榜单")
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        userAdapter.setType(type)
        userAdapter.submitList(rankList)
        mBiding.tvRoseTotal.visibility = View.GONE
        if (type == TYPE_ANGLE){
            mBiding.tvNickName.text = "本场天使榜单"
        }else{
            mBiding.tvNickName.text = "本场歌手榜单"
        }
        userAdapter.isStateViewEnable = rankList.isEmpty()
        userAdapter.addOnItemChildClickListener(R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildClickListener<AngleRankInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<AngleRankInfo, *>,
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
        const val TYPE_SONG = 1
        const val TYPE_ANGLE = 2
        @JvmStatic
        fun showDialog(
            mContext: Context, rankList: List<AngleRankInfo>,type:Int = TYPE_ANGLE
        ): RoomAngleRankPop {
            val matchPop = RoomAngleRankPop(mContext, rankList,type)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
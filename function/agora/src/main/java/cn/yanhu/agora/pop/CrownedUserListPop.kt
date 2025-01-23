package cn.yanhu.agora.pop

import android.content.Context
import android.view.View
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.CrownedUserRankAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.AngleRoomResultInfo
import cn.yanhu.agora.databinding.PopCrownedUserListBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:可加冕用户列表
 */
class CrownedUserListPop(context: Context,val list:MutableList<AngleRankInfo>,val onClickCrownedListener: OnClickCrownedListener,val roomId:String,val type:Int) : BottomPopupView(context){
    companion object {
        const val TYPE_SONG = 1
        const val TYPE_ANGLE = 2
        @JvmStatic
        fun showDialog(
            mContext: Context, rankList: MutableList<AngleRankInfo>,onClickCrownedListener: OnClickCrownedListener,roomId:String,type:Int = TYPE_ANGLE
        ): CrownedUserListPop {
            val matchPop = CrownedUserListPop(mContext, rankList,onClickCrownedListener,roomId,type)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
    private val rangAdapter by lazy{ CrownedUserRankAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_crowned_user_list
    }

    private lateinit var mBinding:PopCrownedUserListBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopCrownedUserListBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无榜单")
        if (type == RoomAngleRankPop.TYPE_ANGLE){
            mBinding.tvTitle.text = "本场天使榜单"
        }else{
            mBinding.tvTitle.text = "本场歌手榜单"
        }
        rangAdapter.stateView = emptyView
        mBinding.rvUser.adapter = rangAdapter
        rangAdapter.submitList(list)
        rangAdapter.isStateViewEnable = list.isEmpty()
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
        rangAdapter.addOnDebouncedChildClick(R.id.tv_crowned,1000,object : BaseQuickAdapter.OnItemChildClickListener<AngleRankInfo>{
            override fun onItemClick(
                adapter: BaseQuickAdapter<AngleRankInfo, *>,
                view: View,
                position: Int
            ) {
                val item = rangAdapter.getItem(position)?:return
                if (item.crowned){
                    return
                }
                request({ agoraRxApi.operateCrowned(roomId,item.userId)},object : OnRequestResultListener<AngleRoomResultInfo>{
                    override fun onSuccess(data: BaseBean<AngleRoomResultInfo>) {
                        item.crowned = true
                        rangAdapter.notifyItemChanged(position)
                        onClickCrownedListener.onCrowned(data.data!!)
                    }
                })
            }
        })
    }

    interface OnClickCrownedListener{
        fun onCrowned(angleResultInfo:AngleRoomResultInfo)
    }
}
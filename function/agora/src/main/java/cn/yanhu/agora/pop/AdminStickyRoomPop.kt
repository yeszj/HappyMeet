package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.StickyTimeAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopAdminStickyRoomBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.StickyInfo
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/12/20
 * desc:管理员直播间置顶
 */
@SuppressLint("ViewConstructor")
class AdminStickyRoomPop(
    context: Context,
    val roomId: String,
    private val timeList: MutableList<StickyInfo>
) : CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, roomId: String, timeList: MutableList<StickyInfo>
        ): AdminStickyRoomPop {
            val matchPop = AdminStickyRoomPop(mContext, roomId, timeList)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    private val stickyTimeAdapter by lazy { StickyTimeAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_admin_sticky_room
    }

    private lateinit var mBinding: PopAdminStickyRoomBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopAdminStickyRoomBinding.bind(popupImplView)
        mBinding.rvTime.adapter = stickyTimeAdapter
        stickyTimeAdapter.submitList(timeList)
        stickyTimeAdapter.setOnItemClickListener { _, _, position ->
            stickyTimeAdapter.setSelectPosition(
                position
            )
        }
        mBinding.tvCancel.setOnSingleClickListener { dismiss() }
        mBinding.tvSure.setOnSingleClickListener {
            stickyRoom()
            dismiss()
        }
    }

    private fun stickyRoom() {
        val selectItem = stickyTimeAdapter.getSelectItem() ?: return
        request({ agoraRxApi.setTop(roomId,selectItem.id)},object : OnRequestResultListener<Boolean>{
            override fun onSuccess(data: BaseBean<Boolean>) {
                showToast("已置顶")
            }
        })
    }
}
package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import com.lxj.xpopup.core.BottomPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopHeartCpUserInviteBinding
import cn.huanyuan.sweetlove.ui.event.cp.InviteUserAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.listener.OnSendSeatInviteListener
import cn.yanhu.agora.pop.LiveRoomOnlineUserPop
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.footer.BallPulseFooter

/**
 * @author: zhengjun
 * created: 2026/3/24
 * desc:
 */
@SuppressLint("ViewConstructor")
class HeartCpUserInvitePop(context: Context, val activityId: String) : BottomPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            activityId: String
        ): HeartCpUserInvitePop {
            val matchPop = HeartCpUserInvitePop(mContext, activityId)
            val builder = XPopup.Builder(mContext)
            builder.autoOpenSoftInput(false)
                .autoFocusEditText(false)
                .enableDrag(false).asCustom(matchPop)
                .show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_heart_cp_user_invite
    }

    private lateinit var mBinding: PopHeartCpUserInviteBinding
    private var page = 1
    private val userAdapter by lazy { InviteUserAdapter() }
    override fun onCreate() {
        super.onCreate()
        mBinding = PopHeartCpUserInviteBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无在线用户")
        userAdapter.stateView = emptyView
        mBinding.rvUser.adapter = userAdapter

        userAdapter.addOnDebouncedChildClick(R.id.ivSelect) { adapter, _, position ->
            userAdapter.setSelectPosition(position)
            if (!mBinding.tvSure.isEnabled) {
                mBinding.tvSure.isEnabled = true
                mBinding.tvSure.setBackgroundResource(cn.yanhu.commonres.R.drawable.common_shape_click_bg_r20)
            }
        }

        getUserList()
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
        mBinding.refresh.setRefreshFooter(BallPulseFooter(context))
        mBinding.refresh.setOnRefreshListener {
            page = 1
            getUserList()
        }
        mBinding.refresh.setOnLoadMoreListener {
            page++
            getUserList()
        }
        mBinding.tvSure.setOnSingleClickListener {
            val selectUser = userAdapter.getSelectUser()
            if (selectUser == null) {
                showToast("请选择用户")
            } else {
                EmMsgManager.sendInviteBindCpMessage(activityId,selectUser)
                showToast("发送邀请成功，等待对方同意")
                dismiss()
            }
        }
        mBinding.etContent.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                page = 1
                getUserList()
            }
        })
    }

    private fun getUserList() {
        val searchContent = mBinding.etContent.text.toString().trim()
        request(
            { agoraRxApi.getHeartInviteUserList(activityId, page,searchContent) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (page == 1) {
                        userAdapter.isStateViewEnable = userList.isEmpty()
                        userAdapter.submitList(userList)
                        mBinding.refresh.finishRefresh()
                    } else {
                        userAdapter.addAll(userList)
                        if (userList.size < 10) {
                            mBinding.refresh.finishLoadMoreWithNoMoreData()
                        } else {
                            mBinding.refresh.finishLoadMore()
                        }
                    }
                }
            })
    }
}
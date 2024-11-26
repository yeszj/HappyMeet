package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.FriendsSelectAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopSelectFriendsBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.bean.response.FriendsResponse
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.footer.BallPulseFooter

/**
 * @author: zhengjun
 * created: 2024/11/22
 * desc:
 */
@SuppressLint("ViewConstructor")
class FriendsSelectPop(
    context: Context,
    val list: MutableList<SameCityUserInfo>,
    private val onSelectFriendListener: OnSelectFriendListener
) :
    BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            list: MutableList<SameCityUserInfo>, onSelectFriendListener: OnSelectFriendListener
        ): FriendsSelectPop {
            val matchPop = FriendsSelectPop(mContext, list, onSelectFriendListener)
            val builder = XPopup.Builder(mContext)
                .enableDrag(false)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }

    private val friendsSelectAdapter by lazy { FriendsSelectAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_select_friends
    }

    private lateinit var mBinding: PopSelectFriendsBinding
    private var page = 1
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSelectFriendsBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无好友")
        friendsSelectAdapter.stateView = emptyView
        mBinding.rvUser.adapter = friendsSelectAdapter
        friendsSelectAdapter.isStateViewEnable = list.size <= 0
        friendsSelectAdapter.submitList(list)
        friendsSelectAdapter.setOnItemClickListener { _, _, position ->
            mBinding.tvSend.alpha = 1f
            friendsSelectAdapter.setSelectPosition(
                position
            )
        }
        mBinding.refresh.setRefreshFooter(BallPulseFooter(context))
        mBinding.refresh.setOnRefreshListener {
            page = 1
            getFriendList()
        }
        mBinding.refresh.setOnLoadMoreListener {
            page++
            getFriendList()
        }
        mBinding.ivClose.setOnSingleClickListener {
            dismiss()
        }
        mBinding.tvSend.setOnSingleClickListener {
            if (friendsSelectAdapter.getSelectPosition() == -1) {
                showToast("请选择赠送的好友")
            } else {
                onSelectFriendListener.onSelectUser(
                    friendsSelectAdapter.getItem(
                        friendsSelectAdapter.getSelectPosition()
                    )!!
                )
                dismiss()
            }
        }
    }

    private fun getFriendList() {
        request({ agoraRxApi.getFriendList(page) },
            object : OnRequestResultListener<FriendsResponse> {
                override fun onSuccess(data: BaseBean<FriendsResponse>) {
                    val friendsResponse = data.data ?: return
                    val friendList = friendsResponse.list
                    if (page == 1) {
                        friendsSelectAdapter.isStateViewEnable = list.size <= 0
                        friendsSelectAdapter.submitList(friendList)
                        mBinding.refresh.finishRefresh()
                    } else {
                        friendsSelectAdapter.addAll(friendList)
                        if (friendList.size < 10) {
                            mBinding.refresh.finishLoadMoreWithNoMoreData()
                        } else {
                            mBinding.refresh.finishLoadMore()
                        }
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (page == 1) {
                        mBinding.refresh.finishRefresh()
                    } else {
                        mBinding.refresh.finishLoadMore()
                    }
                }
            })
    }


    interface OnSelectFriendListener {
        fun onSelectUser(userInfo: SameCityUserInfo)
    }

}
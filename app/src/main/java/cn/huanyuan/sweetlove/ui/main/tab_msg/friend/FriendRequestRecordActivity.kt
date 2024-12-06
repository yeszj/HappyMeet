package cn.huanyuan.sweetlove.ui.main.tab_msg.friend

import android.content.Context
import android.content.Intent
import cn.huanyuan.sweetlove.databinding.ActivityFriendRequestRecordBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.ui.main.tab_msg.adapter.FriendsRequestAdapter
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/12/4
 * desc:
 */
class FriendRequestRecordActivity : BaseActivity<ActivityFriendRequestRecordBinding, MainViewModel>(
    R.layout.activity_friend_request_record,
    MainViewModel::class.java
) {
    private val friendRequestAdapter by lazy { FriendsRequestAdapter() }
    private var page: Int = 1
    override fun initData() {
        setStatusBarStyle(false)
        friendRequestAdapter.stateView = getEmptyView()
        mBinding.recyclerView.adapter = friendRequestAdapter
        requestData()
    }

    override fun initListener() {
        super.initListener()
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_NEW_FRIEND_REQUEST) {
                page = 1
                requestData()
            }
        }
        val listener =
            BaseQuickAdapter.OnItemChildClickListener<SameCityUserInfo> { _, view, position ->
                if (view.id == R.id.tv_pass) {
                    DialogUtils.showConfirmDialog("温馨提示", {
                        operateRequest(position, 1)
                    }, {
                    }, "成为好友后文字聊天免费，确认通过吗？")
                } else if (view.id == R.id.tv_refuse) {
                    operateRequest(position, -1)
                }
            }
        friendRequestAdapter.addOnItemChildClickListener(R.id.tv_pass, listener)
        friendRequestAdapter.addOnItemChildClickListener(R.id.tv_refuse, listener)
    }

    private fun operateRequest(position: Int, status: Int) {
        val item = friendRequestAdapter.getItem(position) ?: return
        mViewModel.operateRequest(
            item.id.toString(),
            status,
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    if (status==1){
                        ChatUserInfoManager.updateIsFriend(item.userId,true)
                        EmMsgManager.sendApplyFriend(item.userId,ImUserManager.getSelfUserInfo().nickName,item.nickName)
                    }
                    LiveEventBus.get<Boolean>(EventBusKeyConfig.ADD_FRIEND_STATE).post(status == 1)
                    friendRequestAdapter.removeAt(position)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    showToast(msg)
                }
            })
    }

    override fun initRefresh() {
        super.initRefresh()
        mBinding.refresh.setOnRefreshListener {
            page = 1
            requestData()
        }
        mBinding.refresh.setOnLoadMoreListener {
            page++
            requestData()
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRequestList(page)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.requestFriendObservable.observe(this) { it ->
            parseState(it, {
                val list = it.list
                if (page == 1) {
                    friendRequestAdapter.isStateViewEnable = list.size == 0
                    friendRequestAdapter.submitList(list)
                } else {
                    friendRequestAdapter.addAll(list)
                }
                setDataLoadFinish(page, list.size, mBinding.refresh)
            }, {
                endLoad(page, mBinding.refresh)
            })
        }
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, FriendRequestRecordActivity::class.java))
        }
    }
}
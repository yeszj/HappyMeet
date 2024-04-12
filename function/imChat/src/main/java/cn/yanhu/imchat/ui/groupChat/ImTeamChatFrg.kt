package cn.yanhu.imchat.ui.groupChat

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.CustomFontTextView
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.RecyclerViewScrollHelper
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.GroupChatRoomAdapter
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.databinding.FrgImTeamChatBinding
import cn.yanhu.imchat.pop.TeamOnLineUserPop
import cn.yanhu.imchat.ui.chatSetting.GroupChatSettingActivity
import cn.yanhu.imchat.custom.chat.CustomFunChatTeamFragment
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.team.TeamServiceObserver
import com.netease.nimlib.sdk.team.model.Team
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import kotlin.math.abs


/**
 * @author: zhengjun
 * created: 2024/3/28
 * desc:
 */
class ImTeamChatFrg : BaseFragment<FrgImTeamChatBinding, ImChatViewModel>(
    R.layout.frg_im_team_chat, ImChatViewModel::class.java
) {
    private var chatFragment: CustomFunChatTeamFragment? = null
    private var groupId: String = ""
    private var isAnim: Boolean = false
    private var userViewHeight = 0
    private val roomAdapter by lazy { GroupChatRoomAdapter() }
    override fun initData() {
        groupId = requireArguments().getString(RouterConstant.CHAT_ID_KRY).toString()
        if (TextUtils.isEmpty(groupId)) {
            mContext.finish()
            return
        }
        mBinding.rvRoom.adapter = roomAdapter
        chatFragment = CustomFunChatTeamFragment()
        chatFragment?.setInitListener(object : CustomFunChatTeamFragment.OnViewInitListener {
            override fun initFinish() {
                registerChatRvScrollListener()
            }
        })
        ChatUiConfig.initConfig()
        chatFragment?.arguments = arguments
        addFragment(chatFragment)
        KeyboardUtils.registerSoftInputChangedListener(mContext) {
            if (it > ScreenUtils.getAppScreenHeight() * 0.15) {
                hideTopUserView()
            } else {
                showTopUserView()
            }
        }
        NIMClient.getService(TeamServiceObserver::class.java)
            .observeTeamRemove(teamRemoveObserver, true)
    }

    // 创建群组被移除的观察者。在退群，被踢，群被解散时会收到该通知。
    private val teamRemoveObserver: Observer<Team> = Observer<Team> {
        showToast("已退群")
        mContext.finish()
    }

    private var onlineTagView:CustomFontTextView?=null
    override fun initListener() {
        super.initListener()
        onlineTagView = LayoutInflater.from(context).inflate(R.layout.view_online_tag, null) as CustomFontTextView
        onlineTagView?.setOnSingleClickListener {
            TeamOnLineUserPop.showPop(mContext,groupId)
        }
        mBinding.titleBar.setCustomLeftView(onlineTagView!!)
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                mContext.finish()
            }

            override fun rightButtonOnClick(v: View?) {
                GroupChatSettingActivity.lunch(mContext, groupId)
            }
        })
    }

    private fun registerChatRvScrollListener() {
        RecyclerViewScrollHelper().attachRecyclerView(
            chatFragment!!.chatView.messageListView,
            object : RecyclerViewScrollHelper.Callback {

                override fun onScrolledToDown(dy: Int) {
                    //下滑
                    showTopUserView(dy)
                }

                override fun onScrolledToUp(dy: Int) {
                    //上滑
                    hideTopUserView(dy)
                }
            })
    }

    private fun hideTopUserView(dy: Int = 60) {
        if (!isAnim && mBinding.rvRoom.visibility == View.VISIBLE && abs(dy) > 50) {
            isAnim = true
            userViewHeight = mBinding.rvRoom.height
            AnimManager.createDropAnimator(
                mBinding.rvRoom, mBinding.rvRoom.height, 0, 500
            )
            ThreadUtils.getMainHandler().postDelayed({
                mBinding.rvRoom.visibility = View.GONE
                isAnim = false
            }, 500)
        }
    }

    private fun showTopUserView(dy: Int = 60) {
        if (!isAnim && mBinding.rvRoom.visibility == View.GONE && abs(dy) > 50) {
            isAnim = true
            mBinding.rvRoom.visibility = View.VISIBLE
            AnimManager.createDropAnimator(
                mBinding.rvRoom, 0, userViewHeight, 500
            )
            ThreadUtils.getMainHandler().postDelayed({
                isAnim = false
            }, 500)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getImGroupInfo(groupId)
    }


    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.groupPageInfoObserver.observe(this) { it ->
            parseState(it, {
                mBinding.titleBar.setLeftTitleName(it.groupName)
                val groupUserRoomList = it.groupUserRoomList
                onlineTagView?.text = it.onlineCount.toString()+"人在线"
                if (groupUserRoomList.size > 0) {
                    roomAdapter.submitList(groupUserRoomList)
                    mBinding.rvRoom.visibility = View.VISIBLE
                } else {
                    mBinding.rvRoom.visibility = View.GONE
                }
                if (it.isVisitor()) {
                    mBinding.vgAddFriendTips.visibility = View.VISIBLE
                    mBinding.tvAddFriend.text = "立即加入丨${it.needRoseNum}玫瑰"
                } else {
                    mBinding.vgAddFriendTips.visibility = View.GONE
                }
            })
        }
    }


    fun onNewIntent(intent: Intent?) {
        chatFragment!!.onNewIntent(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NIMClient.getService(TeamServiceObserver::class.java)
            .observeTeamRemove(teamRemoveObserver, false)
    }
}
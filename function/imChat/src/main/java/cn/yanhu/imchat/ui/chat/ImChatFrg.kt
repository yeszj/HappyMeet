package cn.yanhu.imchat.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.RecyclerViewScrollHelper
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.databinding.FrgImChatBinding
import cn.yanhu.imchat.custom.chat.CustomFunChatP2PFragment
import cn.yanhu.imchat.ui.chatSetting.UserChatSettingActivity
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.netease.yunxin.kit.corekit.im.model.UserInfo
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import kotlin.math.abs

/**
 * @author: zhengjun
 * created: 2024/2/5
 * desc:
 */
@Suppress("DEPRECATION")
class ImChatFrg : BaseFragment<FrgImChatBinding, ImChatViewModel>(
    R.layout.frg_im_chat,
    ImChatViewModel::class.java
) {
    private var chatFragment: CustomFunChatP2PFragment? = null
    private var userId: String = ""
    private var isAnim: Boolean = false
    private var userViewHeight = 0
    override fun initData() {
        val userInfo = requireArguments().getSerializable(RouterConstant.CHAT_KRY) as UserInfo?
        userId = requireArguments().getString(RouterConstant.CHAT_ID_KRY).toString()
        if (userInfo == null && TextUtils.isEmpty(userId)) {
            mContext.finish()
            return
        }
        chatFragment = CustomFunChatP2PFragment()
        chatFragment?.setInitListener(object : CustomFunChatP2PFragment.OnViewInitListener {
            override fun initFinish() {
                registerChatRvScrollListener()
            }
        })
        ChatUiConfig.initConfig()
        chatFragment?.arguments = arguments
        addFragment(chatFragment)
        KeyboardUtils.registerSoftInputChangedListener(mContext) {
            if (it>ScreenUtils.getAppScreenHeight()*0.15){
                hideTopUserView()
            }else{
                showTopUserView()
            }
        }
    }
    override fun initListener() {
        super.initListener()
        val onlineTagView = LayoutInflater.from(context).inflate(R.layout.view_online_tag, null)
        mBinding.titleBar.setCustomLeftView(onlineTagView)
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                mContext.finish()
            }

            override fun rightButtonOnClick(v: View?) {
                UserChatSettingActivity.lunch(mContext,userInfo)
            }

        })
    }

    private fun registerChatRvScrollListener() {
        RecyclerViewScrollHelper().attachRecyclerView(chatFragment!!.chatView.messageListView,
            object : RecyclerViewScrollHelper.Callback {

                override fun onScrolledToDown(dy:Int) {
                    //下滑
                    showTopUserView(dy)
                }

                override fun onScrolledToUp(dy:Int) {
                    //上滑
                    hideTopUserView(dy)
                }
            })
    }

    private fun hideTopUserView(dy: Int = 60) {
        if (!isAnim && mBinding.userView.visibility == View.VISIBLE && abs(dy) > 50) {
            isAnim = true
            userViewHeight = mBinding.userView.height
            AnimManager.createDropAnimator(
                mBinding.userView,
                mBinding.userView.height,
                0,
                500
            )
            ThreadUtils.getMainHandler().postDelayed({
                mBinding.userView.visibility = View.GONE
                isAnim = false
            }, 500)
        }
    }

    private fun showTopUserView(dy: Int = 60) {
        if (!isAnim && mBinding.userView.visibility == View.GONE && abs(dy) > 50) {
            isAnim = true
            mBinding.userView.visibility = View.VISIBLE
            AnimManager.createDropAnimator(
                mBinding.userView,
                0,
                userViewHeight,
                500
            )
            ThreadUtils.getMainHandler().postDelayed({
                isAnim = false
            }, 500)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getUserInfo(userId)
    }

    private var userInfo:UserDetailInfo?=null
    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.userInfoObserver.observe(this) { it ->
            parseState(it, {
                userInfo = it
                mBinding.titleBar.setLeftTitleName(it.nickName)
                if (it.isOnline){
                    mBinding.titleBar.getLeftView()?.visibility = View.VISIBLE
                }else{
                    mBinding.titleBar.getLeftView()?.visibility = View.INVISIBLE
                }
                if (it.isFriend) {
                    mBinding.vgAddFriendTips.visibility = View.GONE
                } else {
                    mBinding.vgAddFriendTips.visibility = View.VISIBLE
                    mBinding.tvAddFriend.text = "加好友丨${it.needRoseNum}玫瑰"
                }
                mBinding.userView.setChatUserInfo(it)
            })
        }
    }


    fun onNewIntent(intent: Intent?) {
        chatFragment!!.onNewIntent(intent)
    }

}
package cn.huanyuan.sweetlove.ui.userinfo

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityUserHomePageBinding
import cn.huanyuan.sweetlove.ui.userinfo.adapter.UserHomePageHeadAdapter
import cn.huanyuan.sweetlove.ui.userinfo.edit.EditUserInfoActivity
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.OperateInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.pop.CommonOperatePop
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.dynamic.adapter.DynamicAdapter
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImChatManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter4.QuickAdapterHelper
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
@Route(path = RouterPath.ROUTER_USER_HOME_PAGE)
class UserHomePageActivity : BaseActivity<ActivityUserHomePageBinding, UserViewModel>(
    R.layout.activity_user_home_page,
    UserViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private val dynamicAdapter by lazy { DynamicAdapter() }
    private val homePageHeadAdapter by lazy { UserHomePageHeadAdapter() }
    private var page: Int = 1
    private lateinit var userId: String
    private var userInfo: UserDetailInfo? = null
    override fun initData() {
        setFullScreenStatusBar(true)
        userInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as UserDetailInfo?

        helper = QuickAdapterHelper.Builder(dynamicAdapter)
            .build()
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        dynamicAdapter.setIsHomePage(true)
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(0, homePageHeadAdapter.apply {
        })
        userId = if (userInfo != null) {
            bindBasicInfo(userInfo!!)
            userInfo!!.userId
        } else {
            intent.getStringExtra(IntentKeyConfig.ID).toString()
        }
        requestData()
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, true, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    page = 1
                    requestData()
                }

                override fun onLoadMore() {
                    page++
                    requestData()
                }

            })
    }

    override fun initListener() {
        super.initListener()
        addOnScrollListener()
        mBinding.ivPub.setOnSingleClickListener {
            RouteIntent.lunchPubDynamic()
        }
        mBinding.ivBack.setOnSingleClickListener { finish() }
        mBinding.vgChat.setOnSingleClickListener { //跳转到单聊界面
            ImChatManager.toImChatPage(mContext, userId)
        }
        mBinding.vgRoom.setOnSingleClickListener {
            userInfo?.apply {
                LiveRoomManager.toLiveRoomPage(mContext,this.roomId.toString())
            }
        }
        mBinding.ivEdit.setOnSingleClickListener {
            EditUserInfoActivity.lunch(mContext)
        }
        mBinding.ivMore.setOnSingleClickListener {
            showOperatePop()
        }
        homePageHeadAdapter.addOnItemChildClickListener(
            R.id.iv_guardFrame
        ) { _, _, _ ->
            GuardRankActivity.lunch(mContext, userId)
        }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_USER_INFO).observe(this) {
            requestData()
        }
    }

    private fun showOperatePop(): CommonOperatePop {
        val list = mutableListOf<OperateInfo>()
        if (userInfo?.isFriend == true){
            list.add(OperateInfo("解除好友", cn.yanhu.commonres.R.color.cl_common, -1))
        }else{
            list.add(OperateInfo("添加好友", cn.yanhu.commonres.R.color.cl_common, 0))
        }
        list.add(OperateInfo("拉黑", cn.yanhu.commonres.R.color.cl_common, 1))
        list.add(OperateInfo("举报", cn.yanhu.commonres.R.color.colorTextRed, 2))
        return CommonOperatePop.showDialog(
            mContext,
            list,
            object : CommonOperatePop.OnClickItemListener {
                override fun onClickItem(operateInfo: OperateInfo) {
                    when (operateInfo.type) {
                        1 -> {
                            ImUserManager.setUserBlack(true, userId)
                        }
                        0 -> {
                            //添加好友
                            if (userInfo?.addFriendWay==0){
                                mViewModel.addFriend(userId)
                            }else{
                                showAddFriendPop()
                            }
                        }
                        -1 -> {
                            //解除好友
                            showCancelFriendTip()
                        }
                        else -> {
                            RouteIntent.lunchReportPage(userId)
                        }
                    }
                }
            })
    }


    private fun showAddFriendPop(): BasePopupView {
        return DialogUtils.showConfirmDialog(
            "添加好友",
            {
                mViewModel.becomeFriendRose(userId)
            },
            {
            },
            content = "是否同意花费${userInfo?.needRoseNum}玫瑰，添加好友？",
            cancel = "取消",
            confirm = "加好友",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }

    private fun showCancelFriendTip() {
        DialogUtils.showConfirmDialog("温馨提示", {
            mViewModel.cancelFriends(userId)
        }, {

        }, "解除好友后文字聊天不再免费，确认解除吗？")
    }



    private fun addOnScrollListener() {
        mBinding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalScrollY = recyclerView.computeVerticalScrollOffset()
                if (totalScrollY < 0) {
                    setStatusBarStyle(true)
                    mBinding.vgTitle.setBackgroundColor(Color.TRANSPARENT)
                    return
                }
                val toolbarHeight = mBinding.vgTitle.bottom * 1.5f
                if (totalScrollY.compareTo(toolbarHeight) <= 0) {
                    val scale = totalScrollY / toolbarHeight
                    val alpha = (scale * 255).toInt()
                    mBinding.vgTitle.setBackgroundColor(Color.argb(alpha, 255, 255, 255));
                    if (totalScrollY <= 45) {
                        setStatusBarStyle(true)
                        mBinding.vgTitle.setBackgroundColor(Color.TRANSPARENT)
                    }
                } else {
                    setStatusBarStyle(false)
                    mBinding.vgTitle.setBackgroundColor(Color.WHITE)
                }
            }
        })
    }

    override fun requestData() {
        super.requestData()
       // getDynamicByUserId()
        mViewModel.getUserInfo(userId,userInfo!=null)
    }

    private fun getDynamicByUserId() {
        mViewModel.getDynamicByUserId(page, userId,userInfo!=null)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.cancelFriendObservable.observe(this){
            parseState(it,{
                ChatUserInfoManager.updateIsFriend(userId,false)
                LiveEventBus.get<Boolean>(EventBusKeyConfig.ADD_FRIEND_STATE).post(true)
                userInfo?.isFriend = false

                showToast("已取消")
            })
        }
        mViewModel.addFriendObservable.observe(this){ it ->
            parseState(it,{
                showToast("好友请求已发送～")
            },{
                showToast(it.msg)
            })
        }
        mViewModel.addFriendRoseObservable.observe(this) { it ->
            parseState(it, {
                userInfo?.isFriend = true
                showToast("添加好友成功")
                ChatUserInfoManager.saveUserInfo(userInfo)
                EmMsgManager.sendCmdMessagePeople(userId, CmdMsgTypeConfig.ADD_FRIEND, null)
            },{
                if (it.code == ErrorCode.CODE_NO_BALANCE){
                    ApplicationProxy.instance.showRechargePop(mContext, true)
                }else{
                    showToast(it.msg)
                }
            })
        }
        mViewModel.dynamicObservable.observe(this) { it ->
            parseState(it, {
                if (page == 1) {
                    dynamicAdapter.submitList(it)
                } else {
                    dynamicAdapter.addAll(it)
                }
                setDataLoadFinish(page, it.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
        mViewModel.userDetailObservable.observe(this) { it ->
            parseState(it, {
                userInfo = it
                bindBasicInfo(it)
                setDataLoadFinish(page, 0, mBinding.refreshLayout)
            },{
                if (it.code == ErrorCode.HAS_BLACK){
                    finish()
                }
            })
        }
    }

    private fun bindBasicInfo(it: UserDetailInfo) {
        if (it.thumbnail.isNotEmpty()) {
            it.coverImg = it.thumbnail[0]
        }
        if (it.roomId == 0) {
            ViewUtils.setMarginLeft(
                mBinding.vgChat,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_16)
            )
        } else {
            ViewUtils.setMarginLeft(
                mBinding.vgChat,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_6)
            )
        }
        mBinding.userinfo = it
        homePageHeadAdapter.item = it
        mBinding.isSelf = AppCacheManager.userId == it.userId
    }
}
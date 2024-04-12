package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.loadinglayout.LoadingManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.loading.CommonLoadingCallBack
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.GroupMemberAdapter
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.GroupOnlineData
import cn.yanhu.imchat.databinding.PopTeamOnlineUserListBinding
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ScreenUtils
import com.kingja.loadsir.core.LoadService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/3/29
 * desc:
 */
@SuppressLint("ViewConstructor")
class TeamOnLineUserPop(val context: FragmentActivity, val groupId: String) :
    BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_team_online_user_list
    }

    private var mBinding: PopTeamOnlineUserListBinding? = null
    private val onlineUserAdapter by lazy {
        GroupMemberAdapter()
    }

    override fun onCreate() {
        super.onCreate()
        mBinding = PopTeamOnlineUserListBinding.bind(popupImplView)
        mBinding?.apply {
            rvUser.adapter = onlineUserAdapter
            onlineUserAdapter.setOnItemClickListener { _, _, position ->
                RouteIntent.lunchPersonHomePage(
                    onlineUserAdapter.getItem(position)?.userId
                )
            }
            ivClose.setOnSingleClickListener { dismiss() }
        }
    }

    override fun beforeShow() {
        super.beforeShow()
        initLoadSir()
        LoadingManager.showLoading(loadService, CommonLoadingCallBack::class.java)
    }

    override fun onShow() {
        super.onShow()
        getOnlineUser()
    }

    private fun initLoadSir() {
        val loadSir =
            LoadingManager.initCustomLoading(CommonLoadingCallBack(cn.yanhu.commonres.R.layout.adapter_loading_user_item))
        loadService = loadSir.register(mBinding?.rvUser) {
            getOnlineUser()
        }
    }

    private lateinit var loadService: LoadService<*>
    private fun getOnlineUser() {
        request({ imChatRxApi.getOnlineUser(groupId) },
            object : OnRequestResultListener<GroupOnlineData> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(data: BaseBean<GroupOnlineData>) {
                    val groupOnlineData = data.data
                    groupOnlineData?.apply {
                        if (onlineUserList.size>0){
                            onlineUserAdapter.submitList(onlineUserList)
                            LoadingManager.showContent(loadService)
                        }else{
                            LoadingManager.showEmpty(loadService)
                        }
                        mBinding?.tvTitle?.text = "在线用户(${totalCount})"
                    }

                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    LoadingManager.showError(loadService)
                }
            })
    }

    companion object {
        @JvmStatic
        fun showPop(
            mContext: FragmentActivity,
            groupId: String
        ): TeamOnLineUserPop {
            val onlinePop = TeamOnLineUserPop(mContext, groupId)
            val builder = XPopup.Builder(mContext)
            builder
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.8).toInt())
                .isDestroyOnDismiss(true).asCustom(onlinePop).show()
            return onlinePop
        }
    }
}
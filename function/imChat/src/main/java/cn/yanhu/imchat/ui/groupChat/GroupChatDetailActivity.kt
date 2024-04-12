package cn.yanhu.imchat.ui.groupChat

import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.GroupDetailUserAdapter
import cn.yanhu.imchat.databinding.ActivityGroupChatDetailBinding
import cn.yanhu.imchat.manager.ImChatManager
import cn.yanhu.imchat.pop.CreateGroupPop
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
@Route(path = RouterPath.ROUTER_GROUP_DETAIL)
class GroupChatDetailActivity : BaseActivity<ActivityGroupChatDetailBinding, ImChatViewModel>(
    R.layout.activity_group_chat_detail,
    ImChatViewModel::class.java
) {
    private val groupDetailUserAdapter by lazy { GroupDetailUserAdapter() }
    private var groupId: String = ""

    override fun initData() {
        setFullScreenStatusBar(true)
        groupId = intent.getStringExtra(IntentKeyConfig.GROUP_ID).toString()
        mBinding.rvUser.adapter = groupDetailUserAdapter
        requestData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivBack.setOnSingleClickListener { finish() }
        mBinding.tvModify.setOnSingleClickListener{
            CreateGroupPop.showDialog(mContext,mBinding.groupInfo)
        }
        mBinding.tvSexCount.setOnSingleClickListener{
            GroupMemberActivity.lunch(mContext, groupId)
        }
        mBinding.vgSee.setOnSingleClickListener {
            ImChatManager.toImGroupChatPage(mContext,groupId)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getGroupDetail(groupId)
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refresh, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refresh)
                }
            })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.groupDetailObserver.observe(this) { it ->
            parseState(it, {
                mBinding.groupInfo = it
                if (it.isGroupUser()) {
                    ViewUtils.setMarginRight(
                        mBinding.vgSee,
                        CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_16)
                    )
                } else {
                    ViewUtils.setMarginRight(
                        mBinding.vgSee,
                        CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_6)
                    )
                }
                groupDetailUserAdapter.submitList(it.groupUserList)
            })
        }
    }
}
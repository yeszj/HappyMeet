package cn.yanhu.imchat.ui.chatSetting

import android.content.Context
import android.content.Intent
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.GroupSettingUserAdapter
import cn.yanhu.imchat.adapter.VisitorRuleAdapter
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.databinding.ActivityGroupChatSettingBinding
import cn.yanhu.imchat.manager.ImChatManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.yanhu.imchat.ui.groupChat.GroupMemberActivity
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.StringUtils
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.team.constant.TeamMemberType
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum
import com.netease.nimlib.sdk.team.model.Team
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember
import com.netease.yunxin.kit.chatkit.repo.TeamRepo.queryTeamWithMember
import com.netease.yunxin.kit.corekit.im.IMKitClient.account
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import java.util.Objects

/**
 * @author: zhengjun
 * created: 2024/3/28
 * desc:
 */
class GroupChatSettingActivity : BaseActivity<ActivityGroupChatSettingBinding, ImChatViewModel>(
    R.layout.activity_group_chat_setting,
    ImChatViewModel::class.java
) {
    private var groupId: String = ""
    private var teamInfo: Team? = null
    private val groupUserAdapter by lazy { GroupSettingUserAdapter() }
    private val visitorRuleAdapter by lazy { VisitorRuleAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        initMemberAdapter()
        initVisitorRuleAdapter()

        groupId = intent.getStringExtra(IntentKeyConfig.GROUP_ID).toString()
        setStickTop()
        requestData()
    }

    private fun initMemberAdapter() {
        mBinding.rvMember.adapter = groupUserAdapter
        groupUserAdapter.setOnItemClickListener { _, _, position ->
            RouteIntent.lunchPersonHomePage(
                groupUserAdapter.getItem(position)?.userId
            )
        }
    }

    private fun initVisitorRuleAdapter() {
        mBinding.rvRule.adapter = visitorRuleAdapter
        val stringArray = StringUtils.getStringArray(cn.yanhu.commonres.R.array.visitorRule)
        visitorRuleAdapter.submitList(stringArray.toMutableList())
        visitorRuleAdapter.setOnItemClickListener { _, _, position ->
            visitorRuleAdapter.setSelectPosition(position)
            setVisitorLimit(position)
        }
    }

    private var selectPosition = 0
    private fun setVisitorLimit(position:Int){
        selectPosition = position
        val visitorEnterRule = if (selectPosition==0){
            3
        }else{
            selectPosition
        }
        mViewModel.setUserLimit(groupId,visitorEnterRule,object : OnBooleanResultListener{
            override fun onSuccess() {

            }
        })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getGroupDetail(groupId)
        getTeamInfo()
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvHistory.setOnSingleClickListener {
            teamInfo?.apply {
                ImChatManager.toGroupHistoryPage(mContext, this)
            }
        }
        mBinding.tvNickName.setOnSingleClickListener {
            RouteIntent.lunchGroupDetail(groupId)
        }
        mBinding.tvGroupUserCount.setOnSingleClickListener {
            GroupMemberActivity.lunch(mContext,groupId)
        }
        mBinding.tvExit.setOnSingleClickListener {
            exitGroup()
        }
    }

    private fun exitGroup() {
        mViewModel.exitGroup(groupId, if (isVisitor) 1 else 2, object : OnBooleanResultListener {
            override fun onSuccess() {
                showToast("退出成功")
                finish()
            }
        })
    }

    /**
     * 根据群id获取群信息
     *
     */
    private fun getTeamInfo() {
        queryTeamWithMember(
            groupId,
            Objects.requireNonNull<String?>(account()),
            object : FetchCallback<TeamWithCurrentMember> {
                override fun onSuccess(param: TeamWithCurrentMember?) {
                    teamInfo = param?.team
                    val teamMember = param?.teamMember
                    if (teamMember?.type == TeamMemberType.Owner) {
                        mBinding.vgVisitor.visibility = View.VISIBLE
                        mBinding.rvRule.visibility = View.VISIBLE
                    } else {
                        mBinding.vgVisitor.visibility = View.GONE
                    }
                    setMessageNotify()
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    /**
     *  开启/取消 消息提醒
     */
    private fun setMessageNotify() {
        mBinding.toggleMessageTip.isChecked =
            teamInfo?.messageNotifyType == TeamMessageNotifyTypeEnum.All
        mBinding.toggleMessageTip.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setNotify(
                !isChecked,
                groupId,
                SessionTypeEnum.Team
            )
        }
    }

    private var isVisitor:Boolean = false
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.groupDetailObserver.observe(this) { it ->
            parseState(it, {
                mBinding.tvNickName.text = it.groupName
                GlideUtils.load(mContext, it.groupIcon, mBinding.ivAvatar)
                mBinding.tvGroupUserCount.text = it.groupUserCount.toString()
                groupUserAdapter.submitList(it.groupUserList)
                isVisitor = it.isVisitor()
                if (it.isVisitor()) {
                    mBinding.tvExit.text = "离开群聊"
                } else {
                    mBinding.tvExit.text = "退出群聊"
                }

                setToggleVisitor(it)

            })
        }
    }

    /**
     * 群主设置游客限制
     */
    private fun setToggleVisitor(it: GroupDetailInfo) {
        val visitorEnterRule = it.visitorEnterRule
        if (visitorEnterRule!=GroupBean.VISITOR_PROHIBIT){
            selectPosition = if (visitorEnterRule==GroupBean.VISITOR_ALL){
                0
            }else{
                visitorEnterRule
            }
            visitorRuleAdapter.setSelectPosition(selectPosition)
        }
        mBinding.toggleVisitor.isChecked = it.visitorEnterRule != GroupBean.VISITOR_PROHIBIT
        mBinding.toggleVisitor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mBinding.rvRule.visibility = View.VISIBLE
                setVisitorLimit(selectPosition)
            } else {
                mBinding.rvRule.visibility = View.GONE
            }
        }
    }

    /**
     * 设置/取消群置顶
     */
    private fun setStickTop() {
        val stickTop = ImUserManager.isStickTop(groupId, SessionTypeEnum.Team)
        mBinding.toggleStickTop.isChecked = stickTop
        mBinding.toggleStickTop.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setStickTop(
                isChecked,
                groupId,
                SessionTypeEnum.Team
            )
        }
    }

    companion object {
        fun lunch(context: Context, groupId: String) {
            val intent = Intent(context, GroupChatSettingActivity::class.java)
            intent.putExtra(IntentKeyConfig.GROUP_ID, groupId)
            context.startActivity(intent)
        }
    }
}
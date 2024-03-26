package cn.yanhu.imchat.groupChat

import android.content.Context
import android.content.Intent
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.bean.AttachParamsInfo
import cn.yanhu.baselib.pop.CommonAttachListPopupView.OnAttachSelectListener
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.GroupMemberAdapter
import cn.yanhu.imchat.adapter.GroupMemberAdapter.Companion.TYPE_ADMIN_CHANGE
import cn.yanhu.imchat.adapter.GroupMemberAdapter.Companion.TYPE_ITEM_LONG_CLICK
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupUserInfo
import cn.yanhu.imchat.databinding.ActivityGroupMemberBinding
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupMemberActivity : BaseActivity<ActivityGroupMemberBinding, ImChatViewModel>(
    R.layout.activity_group_member,
    ImChatViewModel::class.java
) {
    private var groupId: String = ""
    private val groupMemberAdapter by lazy { GroupMemberAdapter() }
    private var page = 1
    private var isOwner = false
    override fun initData() {
        setStatusBarStyle(false)
        groupId = intent.getStringExtra(IntentKeyConfig.GROUP_ID).toString()
        groupMemberAdapter.isStateViewEnable = true
        groupMemberAdapter.stateView = getEmptyView()
        mBinding.rvMember.adapter = groupMemberAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getGroupMember(groupId)
    }

    override fun initListener() {
        groupMemberAdapter.setOnItemLongClickListener(object :
            BaseQuickAdapter.OnItemLongClickListener<GroupUserInfo> {
            override fun onLongClick(
                adapter: BaseQuickAdapter<GroupUserInfo, *>,
                view: View,
                position: Int
            ): Boolean {

                if (isOwner) {
                    val item = groupMemberAdapter.getItem(position)
                    operatePosition = position
                    showAttachPop(item,view)
                    return true
                }
                return false
            }
        })
    }

    private var operatePosition:Int = -1
    private fun showAttachPop(item: GroupUserInfo?,view:View) {
        item?.apply {
            if (item.isVisitor() || item.isOwner()){
                return
            }
            val groupAdmin = item.isGroupAdmin()
            val list: MutableList<AttachParamsInfo> = mutableListOf()
            if (groupAdmin) {
                list.add(AttachParamsInfo("取消管理员",  type =TYPE_CANCEL_ADMIN))
            } else {
                list.add(AttachParamsInfo("设为管理员",  type =TYPE_SET_ADMIN))
                list.add(
                    AttachParamsInfo(
                        "踢出群聊",itemColor =  cn.yanhu.baselib.R.color.colorMain,
                       type =  TYPE_SKIP_MEMBER
                    )
                )
            }
            DialogUtils.showCustomAttachViewPop(mContext,view, list, hasShadowBg = false,onSelectListener =  object : OnAttachSelectListener{
                override fun onSelect(position: Int, attachParamsInfo: AttachParamsInfo?) {
                    when (attachParamsInfo?.type) {
                        TYPE_CANCEL_ADMIN , TYPE_SET_ADMIN-> {
                            setAdmin(attachParamsInfo.type,item.userId)
                        }
                        TYPE_SKIP_MEMBER -> {
                            skipUser(userId)
                        }
                    }
                }
            }, xPopupCallback = object : SimpleCallback() {
                override fun beforeDismiss(popupView: BasePopupView?) {
                    super.beforeDismiss(popupView)
                    item.longClick = false
                    groupMemberAdapter.notifyItemChanged(operatePosition, TYPE_ITEM_LONG_CLICK)
                }
                override fun beforeShow(popupView: BasePopupView?) {
                    super.beforeShow(popupView)
                    item.longClick = true
                    groupMemberAdapter.notifyItemChanged(operatePosition, TYPE_ITEM_LONG_CLICK)
                }
            })
        }
    }

    private fun setAdmin(type:Int,userId:String){
        mViewModel.setAdmin(groupId,type,userId,object : OnBooleanResultListener{
            override fun onSuccess() {
                val item = groupMemberAdapter.getItem(operatePosition)
                item?.apply {
                    if (type == TYPE_SET_ADMIN){
                        item.groupRole = GroupBean.ROLE_ADMIN
                        showToast("设置管理员成功")
                    }else{
                        item.groupRole = GroupBean.ROLE_GROUP_USER
                        showToast("取消管理员成功")
                    }
                    groupMemberAdapter.notifyItemChanged(operatePosition,TYPE_ADMIN_CHANGE)
                }
            }
        })
    }

    private fun skipUser(userId:String){
        mViewModel.skipUser(groupId,userId,object : OnBooleanResultListener{
            override fun onSuccess() {
                showToast("已踢出")
                groupMemberAdapter.removeAt(operatePosition)
            }

        })
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, true, mBinding.refresh, object : IRefreshCallBack {
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

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onGetGroupMemberListener()
    }

    private fun onGetGroupMemberListener() {
        mViewModel.groupMemberObserver.observe(this) { it ->
            parseState(it, {
                isOwner = it.groupRole == GroupBean.ROLE_OWNER
                val groupUserList = it.groupUserList
                if (page == 1) {
                    groupMemberAdapter.submitList(groupUserList)
                } else {
                    groupMemberAdapter.addAll(groupUserList)
                }
                setDataLoadFinish(page, groupUserList.size, mBinding.refresh)
            }, {
                endLoad(page, mBinding.refresh)
            })
        }
    }

    companion object {
        const val TYPE_SKIP_MEMBER = 1
        const val TYPE_CANCEL_ADMIN = 3
        const val TYPE_SET_ADMIN = 2
        fun lunch(context: Context, groupId: String) {
            val intent = Intent(context, GroupMemberActivity::class.java)
            intent.putExtra(IntentKeyConfig.GROUP_ID, groupId)
            context.startActivity(intent)
        }
    }
}
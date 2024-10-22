package cn.yanhu.imchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.R
import cn.yanhu.commonres.view.IconTagInfoView
import cn.yanhu.imchat.adapter.ChatUserAvatarAdapter
import cn.yanhu.imchat.databinding.ViewP2pChatTopUserBinding
import cn.yanhu.imchat.manager.ImUserManager

/**
 * @author: zhengjun
 * created: 2024/3/27
 * desc:
 */
class P2pChatTopUserView : LinearLayout {

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private var mBinding: ViewP2pChatTopUserBinding? = null
    private val avatarAdapter by lazy { ChatUserAvatarAdapter() }
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_p2p_chat_top_user, this, true
        )
        mBinding?.apply {
            initAdapter()
            getSelfInfo()
        }
    }

    private fun ViewP2pChatTopUserBinding.getSelfInfo() {
        selfAvatar = ImUserManager.getSelfUserInfo()?.portrait
    }

    private fun ViewP2pChatTopUserBinding.initAdapter() {
        rvAvatar.adapter = avatarAdapter
        avatarAdapter.setOnItemClickListener { _, _, position ->
            val viewHolder =
                avatarAdapter.recyclerView.findViewHolderForAdapterPosition(position) as ChatUserAvatarAdapter.VH
            DialogUtils.showImageViewerDialog(
                viewHolder.binding.ivAvatar, position, avatarAdapter.items.toMutableList()
            ) { popupView, currentPosition ->
                try {
                    val holder =
                        avatarAdapter.recyclerView.findViewHolderForAdapterPosition(
                            currentPosition
                        ) as ChatUserAvatarAdapter.VH
                    popupView.updateSrcView(holder.binding.ivAvatar)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setChatUserInfo(userInfo:UserDetailInfo){
        mBinding?.apply {
            userinfo = userInfo
            avatarAdapter.submitList(userInfo.thumbnail)
            showUserTag(userInfo)
        }
    }

    private fun ViewP2pChatTopUserBinding.showUserTag(userInfo: UserDetailInfo) {
        flowLayoutInfo.removeAllViews()
        userInfo.basicTagInfo.forEach {
            val tagView = IconTagInfoView(context)
            tagView.setTagValue(it)
            flowLayoutInfo.addView(tagView)
        }
    }
}
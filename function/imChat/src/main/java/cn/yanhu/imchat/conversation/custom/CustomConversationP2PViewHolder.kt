package cn.yanhu.imchat.conversation.custom

import android.view.View
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.imchat.R
import cn.yanhu.imchat.bean.ChatUserInfo
import com.blankj.utilcode.util.GsonUtils
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean
import com.netease.yunxin.kit.conversationkit.ui.normal.viewholder.ConversationP2PViewHolder

/**
 * @author: zhengjun
 * created: 2024/1/31
 * desc:
 */
class CustomConversationP2PViewHolder(binding: ConversationViewHolderBinding) :
    ConversationP2PViewHolder(binding) {
    override fun onBindData(data: ConversationBean, position: Int) {
        super.onBindData(data, position)
        val tvOnlineCount = viewBinding.rootView.findViewById<View>(R.id.tv_onlineCount)
        tvOnlineCount.visibility = View.GONE
        val ivOnline = viewBinding.rootView.findViewById<View>(R.id.iv_online)
        val extension = data.infoData.userInfo?.extension
        val chatUserInfo = GsonUtils.fromJson(extension, ChatUserInfo::class.java)
        chatUserInfo?.apply {
            if (this.isOnline()) {
                ivOnline.visibility = View.VISIBLE
            } else {
                ivOnline.visibility = View.INVISIBLE
            }
            if (this.isAuth){
                TextViewDrawableUtils.setDrawableRight(ivOnline.context,viewBinding.nameTv,cn.yanhu.commonres.R.drawable.svg_identify_tag)
            }else{
                TextViewDrawableUtils.setDrawableRight(viewBinding.nameTv,null)
            }
        }
    }
}
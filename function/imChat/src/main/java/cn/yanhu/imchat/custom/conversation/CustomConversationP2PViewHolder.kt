package cn.yanhu.imchat.custom.conversation

import android.view.View
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.R
import cn.yanhu.imchat.manager.ImUserManager
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

        val fromUserInfo = ImUserManager.getUserInfo(data.infoData.fromAccount)
        val extension = fromUserInfo.extension
        val usrInfo = GsonUtils.fromJson(extension,UserDetailInfo::class.java)

        ivOnline.visibility = View.VISIBLE
        usrInfo?.apply {
            if (this.isOnline) {
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
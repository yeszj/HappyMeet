package cn.yanhu.imchat.chat

import android.content.Intent
import android.text.TextUtils
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.databinding.FrgImChatBinding
import com.netease.yunxin.kit.chatkit.ui.`fun`.page.fragment.FunChatP2PFragment
import com.netease.yunxin.kit.corekit.im.model.UserInfo
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant

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
    private var chatFragment: FunChatP2PFragment? = null

    override fun initData() {
        val userInfo = requireArguments().getSerializable(RouterConstant.CHAT_KRY) as UserInfo?
        val accId = requireArguments().getString(RouterConstant.CHAT_ID_KRY)
        if (userInfo == null && TextUtils.isEmpty(accId)) {
            mContext.finish()
            return
        }
        chatFragment = FunChatP2PFragment()
        ChatUiConfig.initConfig()
        chatFragment?.arguments = arguments
        addFragment(chatFragment)
    }

     fun onNewIntent(intent: Intent?) {
        chatFragment!!.onNewIntent(intent)
    }

}
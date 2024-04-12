package cn.yanhu.imchat.ui.chat

import android.content.Intent
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.databinding.ActivityImChatBinding


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
class ImChatActivity : BaseActivity<ActivityImChatBinding, ImChatViewModel>(
    R.layout.activity_im_chat,
    ImChatViewModel::class.java
) {

    private lateinit var imChatFrg: ImChatFrg
    override fun initData() {
        setStatusBarStyle(false)
        imChatFrg  = ImChatFrg()
        imChatFrg.arguments = intent.extras
        addFragment(imChatFrg)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        imChatFrg.onNewIntent(intent)
    }
}
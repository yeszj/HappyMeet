package cn.yanhu.imchat.ui.groupChat

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
class ImTeamChatActivity : BaseActivity<ActivityImChatBinding, ImChatViewModel>(
    R.layout.activity_im_chat,
    ImChatViewModel::class.java
) {

    private lateinit var imChatFrg: ImTeamChatFrg
    override fun initData() {
        setStatusBarStyle(false)
        imChatFrg  = ImTeamChatFrg()
        imChatFrg.arguments = intent.extras
        addFragment(imChatFrg)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        imChatFrg.onNewIntent(intent)
    }
}
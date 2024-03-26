package cn.yanhu.imchat.conversation

import android.os.Bundle
import android.view.View
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.conversation.custom.CustomConversationView
import cn.yanhu.imchat.conversation.custom.CustomViewHolderFactory
import com.jeremyliao.liveeventbus.LiveEventBus
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationFragment

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class ImConversationFragment : ConversationFragment() {
    override fun initView() {
        this.conversationView = this.viewBinding.conversationView
        this.titleBarView = this.viewBinding.titleBar
        this.networkErrorView = this.viewBinding.errorTv
        this.emptyView = this.viewBinding.emptyLayout
        this.setViewHolderFactory(CustomViewHolderFactory())
        this.viewBinding.titleBar.visibility = View.GONE
        this.viewBinding.conversationLine.visibility = View.GONE
        val type = requireArguments().getInt(IntentKeyConfig.CONVERSATIONSHOWTYPE, TYPE_SINGLE_ALL)
        if(this.conversationView is CustomConversationView){
            //showToast("自定义成功")
            if (type == TYPE_GROUP){
                this.viewBinding.emptyTv.text = "暂未加入任何群聊"
            }else{
                this.viewBinding.emptyTv.text = "暂无会话"
            }
            (this.conversationView as CustomConversationView).setShowType(type)
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.SINGLE_INTIMATE_MSG).observe(this) {
            if (type == TYPE_SINGLE_INTIMATE){
                if(it){
                    emptyView.visibility = View.VISIBLE
                }else{
                    emptyView.visibility = View.GONE
                }
            }

        }
    }


    companion object{
        const val TYPE_GROUP = 1
        const val TYPE_SINGLE_ALL = 2
        const val TYPE_SINGLE_INTIMATE = 3
        fun newsInstance(type:Int):ImConversationFragment{
            val conversationFragment = ImConversationFragment()
            val bundle = Bundle()
            bundle.putInt(IntentKeyConfig.CONVERSATIONSHOWTYPE, type)
            conversationFragment.arguments = bundle
            return conversationFragment
        }
    }
}
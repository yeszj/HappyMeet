package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.base.BaseSheetDialog
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.imchat.databinding.DialogChatListBinding
import cn.yanhu.imchat.ui.conversation.IMConversationListFrg
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import cn.yanhu.imchat.R
import com.jeremyliao.liveeventbus.LiveEventBus

@SuppressLint("ViewConstructor")
class ChatListDialog(val context: FragmentActivity) : BaseSheetDialog<DialogChatListBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogChatListBinding {
        return DialogChatListBinding.inflate(inflater, container, false)
    }

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imListFragment = IMConversationListFrg.newsInstance(true,IMConversationListFrg.TYPE_ALL)
        childFragmentManager.beginTransaction()
            .replace(R.id.dg_chat_list_fg, imListFragment).commit()
        binding?.dgCancel?.setOnSingleClickListener {
            dismiss()
        }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_IM_CONVERSATION).observe(
            this
        ) {
            ThreadUtils.getMainHandler().postDelayed({
                KeyboardUtils.hideSoftInput(context)
            },100)
        }
    }


    override fun onStart() {
        super.onStart()
        if (view != null) {
            val parentView = view
            val parent = parentView!!.parent as View
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(parent)
            parentView.measure(0, 0)
            behavior.peekHeight = parentView.measuredHeight
            val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            parent.layoutParams = layoutParams
            BottomSheetBehavior.from(
                dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            ).isHideable =
                false
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(context: FragmentActivity) {
            val chatListDialog = ChatListDialog(context)
            chatListDialog.showNow(context.supportFragmentManager, "show_chat_i_list")
        }
    }
}
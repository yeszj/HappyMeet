package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.imchat.adapter.ChatSelectImageItemAdapter
import cn.yanhu.imchat.bean.ChatFuncInfo
import cn.yanhu.imchat.databinding.PopChatSelectImageBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.imchat.R
/**
 * @author: zhengjun
 * created: 2024/5/23
 * desc:
 */
@SuppressLint("ViewConstructor")
class ChatSelectImagePop(
    context: Context,
    val list: MutableList<ChatFuncInfo>,
    val call: OnResultCallbackListener<LocalMedia>
) : BottomPopupView(context) {
    private val adapter by lazy { ChatSelectImageItemAdapter() }
    private lateinit var mBinding: PopChatSelectImageBinding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_chat_select_image
    }

    override fun onCreate() {
        super.onCreate()
        mBinding = PopChatSelectImageBinding.bind(popupImplView)
        mBinding.rvMenu.adapter = adapter
        adapter.submitList(list)
        adapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<ChatFuncInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<ChatFuncInfo, *>, view: View, position: Int
            ) {
                val item = adapter.getItem(position) ?: return
                if ("取消" == item.name) {
                    dismiss()
                } else {
                    if (!item.hasLock) {
                        when (position) {
                            0 -> {
                                ImageSelectManager.selectPic(
                                    context as FragmentActivity,
                                    false,
                                    type = ImageSelectUtils.TYPE_IMAGE,
                                    call = call
                                )
                            }

                            1 -> {
                                ImageSelectManager.selectPic(
                                    context as FragmentActivity,
                                    false,
                                    type = ImageSelectUtils.TYPE_CAMERA,
                                    call = call
                                )
                            }

                            else -> {
                                ImageSelectManager.selectPic(
                                    context as FragmentActivity,
                                    false,
                                    type = ImageSelectUtils.TYPE_VIDEO,
                                    call = call
                                )

                            }
                        }
                    }
                }
            }

        })
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            list: MutableList<ChatFuncInfo>,
            call: OnResultCallbackListener<LocalMedia>
        ): ChatSelectImagePop {
            val praiseOpenTipsDialog = ChatSelectImagePop(context, list, call)
            val builder = XPopup.Builder(context)
            builder.asCustom(praiseOpenTipsDialog).show()
            return praiseOpenTipsDialog
        }
    }
}
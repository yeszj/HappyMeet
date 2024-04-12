package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.databinding.AdapterChatRoomMsgNoticeItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgTxtItemBinding
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class LiveRoomChatMessageAdapter :
    BaseMultiItemAdapter<ChatRoomMsgInfo>() {
    class VH(
        val binding: AdapterChatRoomMsgNoticeItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterChatRoomMsgTxtItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(ChatRoomMsgInfo.TYPE_NOTICE, object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: ChatRoomMsgInfo?) {
            }
            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterChatRoomMsgNoticeItemBinding =
                    AdapterChatRoomMsgNoticeItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }
        })
        addItemType(ChatRoomMsgInfo.TYPE_TEXT_MSG, object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH2> {
            override fun onBind(holder: VH2, position: Int, item: ChatRoomMsgInfo?) {
                holder.binding.apply {
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterChatRoomMsgTxtItemBinding =
                    AdapterChatRoomMsgTxtItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }

        }).onItemViewType { position, _ ->
            val item = getItem(position)
            item!!.type
        }
    }
}
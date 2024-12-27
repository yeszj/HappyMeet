package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.GiftMsgInfo
import cn.yanhu.agora.databinding.AdapterChatRoomMsgEmojiItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgGiftItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgNoticeItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgTxtItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgWelcomeItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.Spans
import com.blankj.utilcode.util.GsonUtils
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

    class VH3(
        val binding: AdapterChatRoomMsgEmojiItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH4(
        val binding: AdapterChatRoomMsgGiftItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH5(
        val binding: AdapterChatRoomMsgWelcomeItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
    class VH6(
        val binding: AdapterChatRoomMsgNoticeItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
    init {
        addItemType(ChatRoomMsgInfo.ITEM_NEW_ADD_TYPE,object : OnMultiItemAdapterListener<ChatRoomMsgInfo,VH6>{
            override fun onBind(holder: VH6, position: Int, item: ChatRoomMsgInfo?) {
                holder.binding.tvNotice.text = "此版本暂不支持此消息"
            }
            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH6 {
                val binding: AdapterChatRoomMsgNoticeItemBinding =
                    AdapterChatRoomMsgNoticeItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH6(binding)
            }
        })
        addItemType(
            ChatRoomMsgInfo.ITEM_WELCOME_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH5> {
                override fun onBind(holder: VH5, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.apply {
                        userInfo = item?.sendUserInfo
                        executePendingBindings()
                    }
                }

                override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH5 {
                    val binding: AdapterChatRoomMsgWelcomeItemBinding =
                        AdapterChatRoomMsgWelcomeItemBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH5(binding)
                }

                override fun isFullSpanItem(itemType: Int): Boolean {
                    return true
                }
            })
        addItemType(
            ChatRoomMsgInfo.ITEM_GIFT_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH4> {
                override fun onBind(holder: VH4, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.apply {
                        val fromJson = GsonUtils.fromJson(item?.content, GiftMsgInfo::class.java)
                        userInfo = item?.sendUserInfo
                        giftMsgInfo = fromJson
                        executePendingBindings()
                    }

                }

                override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH4 {
                    val binding: AdapterChatRoomMsgGiftItemBinding =
                        AdapterChatRoomMsgGiftItemBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH4(binding)
                }

                override fun isFullSpanItem(itemType: Int): Boolean {
                    return true
                }
            })

        addItemType(
            ChatRoomMsgInfo.ITEM_EMOJI_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH3> {
                override fun onBind(holder: VH3, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.apply {
                        itemLiveRoomMsgEmoji.setAnimationFromUrl(item?.content)
                        itemLiveRoomMsgEmoji.playAnimation()
                        userInfo = item?.sendUserInfo
                        executePendingBindings()
                    }

                }

                override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH3 {
                    val binding: AdapterChatRoomMsgEmojiItemBinding =
                        AdapterChatRoomMsgEmojiItemBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH3(binding)
                }

                override fun isFullSpanItem(itemType: Int): Boolean {
                    return true
                }
            })

        addItemType(
            ChatRoomMsgInfo.ITEM_SYSTEM_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH> {
                override fun onBind(holder: VH, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.tvNotice.text = item?.content
                }

                override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                    val binding: AdapterChatRoomMsgNoticeItemBinding =
                        AdapterChatRoomMsgNoticeItemBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH(binding)
                }
            })
        addItemType(
            ChatRoomMsgInfo.ITEM_DEFAULT_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH2> {
                override fun onBind(holder: VH2, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.apply {
                        msgInfo = item
                        val altUser = msgInfo?.altUser
                        if (altUser != null) {
                            val build =
                                Spans.builder().text("@${altUser.nickName} ")
                                    .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontOrangeColor))
                                    .text(msgInfo?.content)
                                    .build()
                            tvContent.text = build
                        } else {
                            tvContent.text = msgInfo?.content
                        }
                        executePendingBindings()
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
            if (item!!.type>=0 && item.type<=5){
                item.type
            }else{
                -1
            }
        }
    }
}
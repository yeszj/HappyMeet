package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.GiftMsgInfo
import cn.yanhu.agora.databinding.AdapterChatRoomMsgEmojiItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgGiftItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgNoticeItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgRobotItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgTxtItemBinding
import cn.yanhu.agora.databinding.AdapterChatRoomMsgWelcomeItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.widget.spans.Spans
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.lihang.ShadowLayout
import com.zj.dimens.R

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class LiveRoomChatMessageAdapter : BaseMultiItemAdapter<ChatRoomMsgInfo>() {
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

    class VH7(
        val binding: AdapterChatRoomMsgRobotItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(
            ChatRoomMsgInfo.ITEM_NEW_ADD_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH6> {
                override fun onBind(holder: VH6, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.tvNotice.text = "此版本暂不支持此消息"
                    ViewUtils.setMarginVertical(
                        holder.binding.vgNotice,
                        0,
                        CommonUtils.getDimension(R.dimen.dp_10)
                    )
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
                        try {
                            itemLiveRoomMsgEmoji.setAnimationFromUrl(item?.content)
                            itemLiveRoomMsgEmoji.playAnimation()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        userInfo = item?.sendUserInfo
                        setChatBgStyle(item, tvContent, tvChatStyle)
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
                    if (position == 0) {
                        ViewUtils.setMarginTop(
                            holder.binding.vgNotice, CommonUtils.getDimension(R.dimen.dp_10)
                        )
                    } else {
                        ViewUtils.setMarginTop(holder.binding.vgNotice, 0)
                    }
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
            ChatRoomMsgInfo.ITEM_ROBOT_TYPE,
            object : OnMultiItemAdapterListener<ChatRoomMsgInfo, VH7> {
                override fun onBind(holder: VH7, position: Int, item: ChatRoomMsgInfo?) {
                    holder.binding.apply {
                        tvContent.text = item?.content
                        executePendingBindings()
                    }
                }

                override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH7 {
                    val binding: AdapterChatRoomMsgRobotItemBinding =
                        AdapterChatRoomMsgRobotItemBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH7(binding)
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
                            val build = Spans.builder().text("@${altUser.nickName} ")
                                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontOrangeColor))
                                .text(msgInfo?.content).build()
                            tvContent.text = build
                        } else {
                            tvContent.text = msgInfo?.content
                        }
                        setChatBgStyle(item, tvContent, tvChatStyle)
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
            if (item!!.type >= 0 && item.type <= 6) {
                item.type
            } else {
                -1
            }
        }
    }

    private fun setChatBgStyle(item: ChatRoomMsgInfo?, tvContent: View, tvChatStyle: ShadowLayout) {
        val sendUserInfo = item?.sendUserInfo
        val bubbleInfo = sendUserInfo?.bubbleInfo
        if (bubbleInfo != null) {
            val type = bubbleInfo.type
            if ("ninePatch" == type) {
                tvChatStyle.visibility = View.INVISIBLE
                tvContent.setBackgroundResource(cn.yanhu.agora.R.drawable.chatbg)
            } else {
                tvChatStyle.visibility = View.VISIBLE
                tvContent.background = null
                val content = bubbleInfo.content
                val gradientList = content.gradientList
                val startColor = gradientList[0].color
                var centerColor = ""
                var endColor = startColor
                if (gradientList.size == 2) {
                    endColor = gradientList[1].color
                } else if (gradientList.size >= 3) {
                    centerColor = gradientList[1].color
                    endColor = gradientList[2].color
                }
                if (!CommonUtils.isEmpty(centerColor)) {
                    tvChatStyle.setGradientColor(
                        bubbleInfo.content.angle,
                        startColor.toColorInt(),
                        centerColor.toColorInt(),
                        endColor.toColorInt()
                    )
                } else {
                    tvChatStyle.setGradientColor(
                        bubbleInfo.content.angle,
                        startColor.toColorInt(),
                        endColor.toColorInt()
                    )
                }
            }
        } else {
            tvChatStyle.visibility = View.INVISIBLE
            tvContent.setBackgroundResource(cn.yanhu.commonres.R.drawable.black_alpha80_corner_10)
        }
    }
}
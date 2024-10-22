package cn.yanhu.imchat.adapter

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.utils.GifLoadUtils
import cn.yanhu.commonres.view.svg.SvgSoftwareLayerSetter
import cn.yanhu.imchat.databinding.AdapterEmojiTxtItemBinding
import cn.yanhu.imchat.databinding.AdapterMessageExpressionBinding
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.hyphenate.easeui.domain.EaseEmojicon


/**
 * @author: witness
 * created: 2022/12/14
 * desc:
 */
class MessageExpressionAdapter : BaseMultiItemAdapter<EaseEmojicon>() {
    class VH(
        val binding: AdapterMessageExpressionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterEmojiTxtItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(TYPE_EMOJI, object : OnMultiItemAdapterListener<EaseEmojicon, VH> {
            override fun onBind(holder: VH, position: Int, item: EaseEmojicon?) {
                holder.binding.apply {
                    try {
                        if (item == null) {
                            return
                        }
                        if (item.type == EaseEmojicon.Type.NORMAL) {
                            ViewUtils.setViewHeight(
                                vgParent, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_52)
                            )
                            ViewUtils.setViewSize(
                                animExpression,
                                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_28),
                                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_28)
                            )
                        } else {
                            ViewUtils.setViewHeight(
                                vgParent, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_91)
                            )
                            ViewUtils.setViewSize(
                                animExpression,
                                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_75),
                                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_75)
                            )
                        }
                        val iconPath = item.iconPath
                        if (!TextUtils.isEmpty(iconPath)) {
                            if (iconPath.endsWith(".gif")) {
                                GifLoadUtils.loadGif(iconPath, animExpression)
                            } else {
                                Glide.with(context).`as`(
                                    PictureDrawable::class.java
                                ).listener(SvgSoftwareLayerSetter())
                                    .load(item.iconPath)
                                    .into(animExpression)
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterMessageExpressionBinding =
                    AdapterMessageExpressionBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }

        }).addItemType(TYPE_TXT, object : OnMultiItemAdapterListener<EaseEmojicon, VH2> {
            override fun onBind(holder: VH2, position: Int, item: EaseEmojicon?) {
                holder.binding.tvDesc.text = item?.emojiText
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterEmojiTxtItemBinding = AdapterEmojiTxtItemBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
                return VH2(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }

        }).onItemViewType { position, _ ->
            val item = getItem(position)
            if (item?.isTxtDesc == true) {
                TYPE_TXT
            } else {
                TYPE_EMOJI
            }
        }
    }

    companion object {
        const val TYPE_EMOJI = 1
        const val TYPE_TXT = 2
    }
}
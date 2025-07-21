package cn.huanyuan.sweetlove.ui.recommend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterRecommendRoomEmptyBinding
import cn.huanyuan.sweetlove.databinding.AdapterRecommendRoomHeadBinding
import cn.huanyuan.sweetlove.databinding.AdapterRecommendRoomItemBinding
import cn.yanhu.commonres.bean.UserDetailInfo
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2025/7/17
 * desc:
 */
class RecommendRoomAdapter : BaseMultiItemAdapter<UserDetailInfo>() {
    class VH(
        val binding: AdapterRecommendRoomItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterRecommendRoomHeadBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH3(
        val binding: AdapterRecommendRoomEmptyBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(TYPE_ROOM, object : OnMultiItemAdapterListener<UserDetailInfo, VH> {
            override fun onBind(
                holder: VH, position: Int, item: UserDetailInfo?
            ) {
                holder.binding.apply {
                    userInfo = item
                    if ((svgaLoading.tag as OnAttachStateChangeListener?)==null){
                        val attachStateChangeListener =  object : OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                svgaLoading.startAnimation()
                            }
                            override fun onViewDetachedFromWindow(v: View) {
                                svgaLoading.pauseAnimation()
                            }
                        }
                        svgaLoading.addOnAttachStateChangeListener(attachStateChangeListener)
                        svgaLoading.tag = attachStateChangeListener
                    }
                    executePendingBindings()
                }
            }

            override fun onCreate(
                context: Context, parent: ViewGroup, viewType: Int
            ): VH {
                val binding: AdapterRecommendRoomItemBinding =
                    AdapterRecommendRoomItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }

        }).addItemType(TYPE_TITLE, object : OnMultiItemAdapterListener<UserDetailInfo, VH2> {
            override fun onBind(
                holder: VH2, position: Int, item: UserDetailInfo?
            ) {
                holder.binding.apply {
                    if (position == 0) {
                        tvTitle.text = "好友"
                    } else {
                        tvTitle.text = "可能认识的人"
                    }
                }

            }

            override fun onCreate(
                context: Context, parent: ViewGroup, viewType: Int
            ): VH2 {
                val binding: AdapterRecommendRoomHeadBinding =
                    AdapterRecommendRoomHeadBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }

        })
            .addItemType(TYPE_EMPTY, object : OnMultiItemAdapterListener<UserDetailInfo, VH3> {
                override fun onBind(
                    holder: VH3,
                    position: Int,
                    item: UserDetailInfo?
                ) {
                    holder.binding.apply {
                        if (position == 1) {
                            tvEmpty.text = "暂无好友在线..."
                        } else {
                            tvEmpty.text = "暂无可能认识的人在线..."
                        }
                    }
                }

                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): VH3 {
                    val binding: AdapterRecommendRoomEmptyBinding =
                        AdapterRecommendRoomEmptyBinding.inflate(
                            LayoutInflater.from(context), parent, false
                        )
                    return VH3(binding)
                }

            })
            .onItemViewType(object : OnItemViewTypeListener<UserDetailInfo> {
                override fun onItemViewType(position: Int, list: List<UserDetailInfo>): Int {
                    val roomListBean = list[position]
                    val roomId = roomListBean.roomId
                    return if (roomId == -1) {
                        TYPE_TITLE
                    } else if (roomId == -2) {
                        TYPE_EMPTY
                    } else {
                        TYPE_ROOM
                    }
                }
            })
    }

    companion object {
        const val TYPE_ROOM = 1
        const val TYPE_TITLE = 2
        const val TYPE_EMPTY = 3
    }
}
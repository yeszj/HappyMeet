package cn.huanyuan.sweetlove.ui.main.tab_blinddate.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterBlindBannerItemBinding
import cn.huanyuan.sweetlove.databinding.AdapterBlindRoomItemBinding
import cn.huanyuan.sweetlove.databinding.AdapterBlindUserItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.adapter.MyBannerImageAdapter
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.router.PageIntentUtil
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.youth.banner.listener.OnBannerListener

/**
 * @author: zhengjun
 * created: 2024/2/19
 * desc:
 */
class RoomListAdapter(context: FragmentActivity) : BaseMultiItemAdapter<RoomListBean>() {
    class VH(
        val binding: AdapterBlindUserItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterBlindBannerItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class RoomViewHolder(val binding: AdapterBlindRoomItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(TYPE_ROOM, object : OnMultiItemAdapterListener<RoomListBean, VH> {
            override fun onBind(holder: VH, position: Int, item: RoomListBean?) {
                holder.binding.apply {
                    roomBean = item
                    ivStatus.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            ivStatus.startAnimation()
                        }

                        override fun onViewDetachedFromWindow(v: View) {
                            ivStatus.pauseAnimation()
                        }
                    })
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterBlindUserItemBinding = AdapterBlindUserItemBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
                return VH(binding)
            }
        })
        addItemType(
            TYPE_FRIENDS,
            object : OnMultiItemAdapterListener<RoomListBean, RoomViewHolder> {
                override fun onBind(holder: RoomViewHolder, position: Int, item: RoomListBean?) {
                    holder.binding.apply {
                        roomBean = item
                        if (rvAvatar.tag == null) {
                            val roomAvatarAdapter = RoomAvatarAdapter()
                            rvAvatar.adapter = roomAvatarAdapter
                            val linearLayoutManager = LinearLayoutManager(context)
                            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                            linearLayoutManager.stackFromEnd = true//列表再底部开始展示
                            linearLayoutManager.reverseLayout = true//列表翻转
                            rvAvatar.layoutManager = linearLayoutManager
                            rvAvatar.addItemDecoration(object : RecyclerView.ItemDecoration() {
                                override fun getItemOffsets(
                                    outRect: Rect,
                                    view: View,
                                    parent: RecyclerView,
                                    state: RecyclerView.State,
                                ) {
                                    super.getItemOffsets(outRect, view, parent, state)
                                    if (parent.getChildLayoutPosition(view) != roomAvatarAdapter.itemCount - 1) {
                                        outRect.left =
                                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_m_10)
                                    }
                                }
                            })
                            rvAvatar.tag = roomAvatarAdapter
                            roomAvatarAdapter.submitList(item?.roomPortraitList)
                        } else {
                            rvAvatar.adapter = rvAvatar.tag as RecyclerView.Adapter<*>?
                        }
                        ivStatus.addOnAttachStateChangeListener(object :
                            OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                ivStatus.startAnimation()
                            }

                            override fun onViewDetachedFromWindow(v: View) {
                                ivStatus.pauseAnimation()
                            }
                        })
                        executePendingBindings()
                    }
                }

                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): RoomViewHolder {
                    val binding: AdapterBlindRoomItemBinding = AdapterBlindRoomItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                    return RoomViewHolder(binding)
                }
            })
        addItemType(TYPE_BANNER, object : OnMultiItemAdapterListener<RoomListBean, VH2> {
            override fun onBind(holder: VH2, position: Int, item: RoomListBean?) {
                holder.binding.apply {
                    banner.addBannerLifecycleObserver(context)
                    banner.setAdapter(MyBannerImageAdapter(banner, item!!.banners))
                    banner.setOnBannerListener(object : OnBannerListener<BannerBean> {
                        override fun OnBannerClick(data: BannerBean, position: Int) {
                            PageIntentUtil.url2Page(ActivityUtils.getTopActivity(), data.pageUrl)
                        }
                    })
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterBlindBannerItemBinding = AdapterBlindBannerItemBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
                return VH2(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }
        }).onItemViewType(object : OnItemViewTypeListener<RoomListBean> {
            override fun onItemViewType(position: Int, list: List<RoomListBean>): Int {
                val roomListBean = list[position]
                val banners = roomListBean.banners
                return if (banners.isNotEmpty()) {
                    return TYPE_BANNER
                } else {
                    if (roomListBean.roomType < 3) {
                        TYPE_ROOM
                    } else {
                        TYPE_FRIENDS
                    }
                }
            }
        })
    }

    companion object {
        const val TYPE_ROOM = 1
        const val TYPE_BANNER = 2
        const val TYPE_FRIENDS = 3 //交友房
    }

}
package cn.yanhu.dynamic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.dynamic.MomentContentBindUtil
import cn.yanhu.dynamic.OnClickDiscussListener
import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.databinding.AdapterDiscussItemBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.like.LikeButton
import com.like.OnLikeListener
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.manager.MomentManager
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2023/6/30
 * desc:
 */
class DiscussAdapter :
    BaseQuickAdapter<DiscussInfo, DiscussAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: AdapterDiscussItemBinding = AdapterDiscussItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val UPDATE_LIKE_COUNT = "likeCount"
        const val UPDATE_REPLY = "updateReply"
    }

    private var trendsId: String = ""

    private fun AdapterDiscussItemBinding.registerLikeBtnListener(item: DiscussInfo) {
        likeBtn.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton?) {
                MomentManager.praiseDiscuss(trendsId, item.id.toString(), MomentManager.LIKE)
                item.commentIsLike = true
                item.commentLikeCount++
                val indexOf = items.indexOf(item)
                notifyItemChanged(indexOf, UPDATE_LIKE_COUNT)
            }

            override fun unLiked(likeButton: LikeButton?) {
                MomentManager.praiseDiscuss(trendsId, item.id.toString(), MomentManager.UNLIKE)
                item.commentIsLike = false
                item.commentLikeCount--
                val indexOf = items.indexOf(item)
                notifyItemChanged(indexOf, UPDATE_LIKE_COUNT)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun AdapterDiscussItemBinding.bindContent(item: DiscussInfo) {
        if (tvTime.tag == null || tvTime.tag != item.info) {
            if (TextUtils.isEmpty(item.province)){
                tvTime.text = item.commentTime
            }else{
                tvTime.text = "${item.commentTime} · ${item.province}"
            }
            MomentContentBindUtil.setTextEndTextSpan(
                tvContent,
                item.info,
                tvMore,
                clickTextListener = object : MomentContentBindUtil.OnClickTextListener {
                    override fun onClickText() {
                        onClickDiscussListener?.onClickItem(items.indexOf(item), item)
                    }
                }
            )
            tvTime.tag = item.info
        }
    }

    private fun AdapterDiscussItemBinding.bindReplyData(discussInfo: DiscussInfo) {
        val replyAdapter: ReplyAdapter
        if (rvReply.tag == null) {
            replyAdapter = ReplyAdapter()
            initReplyAdapter(replyAdapter)
        } else {
            replyAdapter = rvReply.tag as ReplyAdapter
        }
        replyAdapter.submitList(discussInfo.replayComments)
        if (discussInfo.firstLevelCommentReplayCount >= 1) {
            tvMoreReply.visibility = View.VISIBLE
            tvMoreReply.text = String.format(
                "%s条回复",
                discussInfo.firstLevelCommentReplayCount
            )
            tvMoreReply.setOnSingleClickListener {
                //获取更多回复数据
                getMoreReply(discussInfo, replyAdapter, it)
            }
        } else {
            tvMoreReply.visibility = View.GONE
        }
    }

    private fun AdapterDiscussItemBinding.getMoreReply(
        discussInfo: DiscussInfo,
        replyAdapter: ReplyAdapter,
        it: View
    ) {
        request({
            momentRxApi.getCommentReplay(
                trendsId,
                discussInfo.id.toString(),
                discussInfo.page
            )
        },
            object : OnRequestResultListener<MutableList<DiscussInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<DiscussInfo>>) {
                    discussInfo.page++
                    val discussInfoMutableList = data.data
                    if (discussInfoMutableList != null) {
                        replyAdapter.addAll(discussInfoMutableList)
                        discussInfo.firstLevelCommentReplayCount =
                            discussInfo.firstLevelCommentReplayCount - discussInfoMutableList.size
                        if (discussInfo.firstLevelCommentReplayCount <= 0) {
                            it.visibility = View.GONE
                        } else {
                            tvMoreReply.text = String.format(
                                "%s条回复",
                                discussInfo.firstLevelCommentReplayCount
                            )
                        }
                    } else {
                        it.visibility = View.GONE
                    }
                }
            })
    }

    private fun AdapterDiscussItemBinding.initReplyAdapter(replyAdapter: ReplyAdapter) {
        replyAdapter.setHasStableIds(true)
        rvReply.adapter = replyAdapter
        rvReply.itemAnimator?.changeDuration = 0
        rvReply.tag = replyAdapter
        replyAdapter.setOnItemClickListener { _, _, position ->
            val tag = vgItem.tag as DiscussInfo
            clickReply(replyAdapter, position, tag)
        }
        replyAdapter.addOnItemChildClickListener(R.id.tv_content,
            object : OnItemChildClickListener<DiscussInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<DiscussInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val tag = vgItem.tag as DiscussInfo
                    clickReply(replyAdapter, position, tag)
                }
            })
        replyAdapter.addOnItemChildLongClickListener(R.id.tv_content
        ) { adapter, view, position ->
            val item = replyAdapter.getItem(position)

            DialogUtils.showLongClickCopyDialog(
                view,
                item!!.info,
                object : DialogUtils.OnDeleteListener {
                    override fun startDelete() {
                        //删除回复
                        val id = item.id
                        MomentManager.showDeleteDiscussDialog(
                            id.toString(),
                            item.isSelf(),
                            trendsId,
                            object :
                                OnBooleanResultListener {
                                override fun onSuccess() {
                                    adapter.removeAt(position)
                                }
                            })
                    }

                })
            false
        }
    }

    private var onClickDiscussListener: OnClickDiscussListener? = null
    fun setOnClickTextListener(listener: OnClickDiscussListener) {
        onClickDiscussListener = listener
    }


    private fun clickReply(
        replyAdapter: ReplyAdapter,
        position: Int,
        item: DiscussInfo,
    ) {
        for (discussPosition in this@DiscussAdapter.items.indices) {
            if (this@DiscussAdapter.items[discussPosition].id == item.id) {
                onClickReplyListener?.onClickReply(
                    replyAdapter.getItem(position)!!,
                    discussPosition
                )
                return
            }
        }

    }


    private var onClickReplyListener: OnClickReplyListener? = null
    fun setReplyListener(listener: OnClickReplyListener) {
        onClickReplyListener = listener
    }

    interface OnClickReplyListener {
        fun onClickReply(item: DiscussInfo, position: Int)
    }

    fun setTrendsId(id: String) {
        trendsId = id
    }

    private fun AdapterDiscussItemBinding.bindLikeBtn(item: DiscussInfo) {
        likeBtn.isLiked = item.commentIsLike
        if (item.commentIsLike) {
            tvHeartNum.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.font_pink_color))
        } else {
            tvHeartNum.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor))
        }
        tvHeartNum.text = item.commentLikeCount.toString()
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: DiscussInfo?,
        payloads: List<Any>
    ) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            if (payloads.isNotEmpty()) {
                payloads.forEach {
                    if (it is String && it == UPDATE_LIKE_COUNT) {
                        bindLikeBtn(item)
                    } else if (it is DiscussInfo) {
                        val replyAdapter = rvReply.tag as ReplyAdapter
                        replyAdapter.add(it)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: DiscussInfo?) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            discussInfo = item
            vgItem.tag = item
            bindContent(item)
            bindLikeBtn(item)
            registerLikeBtnListener(item)
            bindReplyData(item)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}
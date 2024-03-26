package cn.yanhu.dynamic.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.utils.VideoUtils
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.databinding.AdapterDynamicItemBinding
import cn.yanhu.dynamic.manager.MomentManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.like.LikeButton
import com.like.OnLikeListener

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
class DynamicAdapter : BaseQuickAdapter<DynamicInfo, DynamicAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterDynamicItemBinding = AdapterDynamicItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: DynamicInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()&&item!=null) {
            val any = payloads[0]
            if (any.toString() == UPDATE_DISCUSS) {
                updateDiscussNum(holder, item)
            } else if (any.toString() == UPDATE_LIKE) {
                updateLikeBtn(holder, item)
            }
        }
    }

    private fun updateLikeBtn(
        holder: VH,
        item: DynamicInfo,
    ) {
        holder.binding.apply {
            bindLikeBtnStatus(item)
            tvHeartNum.text = item.trendsLikeCount.toString()
        }
    }
    
    private fun updateDiscussNum(
        holder: VH,
        item: DynamicInfo,
    ) {
        holder.binding.apply {
            tvDiscussNum.text = item.trendsCommentCount.toString()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: DynamicInfo?) {
        holder.binding.apply {
            dynamicInfo = item
            isPersonHomePage = isHomePage
            if (TextUtils.isEmpty(item?.roomId)){
                avatarView.getAvatarView().setBorderWidth(com.zj.dimens.R.dimen.dp_0)
            }else{
                avatarView.getAvatarView().setBorderWidth(com.zj.dimens.R.dimen.dp_1)
            }
            bindPic(item)

            bindLikeBtnStatus(item)
            likeBtn.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton?) {
                    //关注
                    val tag = likeBtn.tag as DynamicInfo
                    MomentManager.praiseMoment(tag.id.toString(), MomentManager.LIKE)
                }
                override fun unLiked(likeButton: LikeButton?) {
                    //取消关注
                    val tag = likeBtn.tag as DynamicInfo
                    MomentManager.praiseMoment(tag.id.toString(), MomentManager.UNLIKE)
                }
            })
            if(isHomePage){
                ViewUtils.setMarginLeft(tvTime,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4))
            }
            executePendingBindings()
        }
    }

    private fun AdapterDynamicItemBinding.bindLikeBtnStatus(item: DynamicInfo?) {
        likeBtn.isLiked = item?.trendsIsLike == true
        if (item?.trendsIsLike == true) {
            tvHeartNum.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain))
        } else {
            tvHeartNum.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor))
        }
        likeBtn.tag = item
    }

    private fun AdapterDynamicItemBinding.bindPic(item: DynamicInfo?) {
        if (item?.images?.size == 1 && VideoUtils.isVideo(item.images[0])) {
            picGridlayout.visibility = View.GONE
            MomentManager.showSingleImage(context, vgSingleVideo, item)
        } else {
            picGridlayout.visibility = View.VISIBLE
            vgSingleVideo.vgVideo.removeAllViews()
            vgSingleVideo.vgVideoParent.visibility = View.GONE
            picGridlayout.setUrlList(item?.images, item!!)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var isHomePage:Boolean = false
    fun setIsHomePage(isPersonHomePage:Boolean){
        isHomePage = isPersonHomePage
    }

    companion object {
        const val UPDATE_LIKE = "updateLikeBtn"
        const val UPDATE_DISCUSS = "updateDiscuss"
        const val UPDATE_FOLLOW_BTN = "updateFollowBtn"
        const val AUDIT_PASS = "auditPass"
    }
}
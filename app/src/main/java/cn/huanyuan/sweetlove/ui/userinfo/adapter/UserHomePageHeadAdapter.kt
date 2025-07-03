package cn.huanyuan.sweetlove.ui.userinfo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterHomepageHeadBinding
import cn.huanyuan.sweetlove.ui.userinfo.GuardRankActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.view.IconTagInfoView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter4.BaseQuickAdapter.OnItemClickListener
import com.chad.library.adapter4.BaseSingleItemAdapter


/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class UserHomePageHeadAdapter :
    BaseSingleItemAdapter<UserDetailInfo, UserHomePageHeadAdapter.VH>() {
    class VH(var binding: AdapterHomepageHeadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, @SuppressLint("RecyclerView") item: UserDetailInfo?) {
        holder.binding.apply {
            userinfo = item?:return
            if (item.guardInfo==null){
                ivGuardFrame.setImageResource(cn.yanhu.commonres.R.drawable.icon_no_guard)
            }else{
                if (TextUtils.isEmpty(item.guardInfo?.nickName)){
                    ivGuardFrame.setImageResource(cn.yanhu.commonres.R.drawable.icon_no_guard)
                }else{
                    GlideUtils.loadAsDrawable(context, item.guardInfo!!.avatarFrame, object :
                        CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            ivGuardFrame.setImageDrawable(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
                }
            }
            ivGuardFrame.setOnSingleClickListener {
                GuardRankActivity.lunch(context, item.userId)
            }
            vgLovers.setOnSingleClickListener {
               RouteIntent.lunchLoversPage(item.userId)
            }
            when (item.loverInfo?.loversType) {
                4 -> {
                    vgLovers.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_lovers4_tag)
                }
                3 -> {
                    vgLovers.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_lovers3_tag)
                }
                2 -> {
                    vgLovers.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_lovers2_tag)
                }
                else -> {
                    vgLovers.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_lovers1_tag)
                }
            }
            bindAvatar(item)
            bindPersonInfoTag(item)
            bindConditioinTag(item)
            executePendingBindings()
        }
    }


    private fun AdapterHomepageHeadBinding.bindPersonInfoTag(item: UserDetailInfo?) {
        val personInfo = item?.personInfo
        flowLayoutInfo.removeAllViews()
        personInfo?.forEach {
            val tagView = IconTagInfoView(context)
            tagView.setTagValue(it)
            flowLayoutInfo.addView(tagView)
        }
    }

    private fun AdapterHomepageHeadBinding.bindConditioinTag(item: UserDetailInfo?) {
        val friendCondition = item?.friendCondition
        flowLayoutCondition.removeAllViews()
        friendCondition?.forEach {
            val tagView = IconTagInfoView(context)
            tagView.setTagValue(it)
            flowLayoutCondition.addView(tagView)
        }
    }

    private var selectPosition = 0
    private fun AdapterHomepageHeadBinding.bindAvatar(item: UserDetailInfo?) {
        if (item==null){
            return
        }

        var homePageAvatarAdapter = rvAvatar.tag as HomePageAvatarAdapter?
        if (homePageAvatarAdapter == null) {
            homePageAvatarAdapter = HomePageAvatarAdapter()
            homePageAvatarAdapter.setOnItemClickListener(OnItemClickListener { _, _, position ->
                selectPosition = position
                item.coverImg = item.thumbnail[position]
                homePageAvatarAdapter.setSelectPosition(position)
            })

            rvAvatar.adapter = homePageAvatarAdapter
        }
        homePageAvatarAdapter.submitList(item.thumbnail)
        ivCover.setOnSingleClickListener {
            if (item.thumbnail.isEmpty()){
                DialogUtils.showImageViewerDialog(ivCover,item.coverImg)
            }else{
                var viewHolder =
                    homePageAvatarAdapter.recyclerView.findViewHolderForAdapterPosition(
                        selectPosition
                    ) as HomePageAvatarAdapter.VH
                DialogUtils.showImageViewerDialog(viewHolder.binding.ivAvatar,selectPosition, item.thumbnail.toMutableList()
                ) { popupView, position ->
                    try {
                        viewHolder =
                            homePageAvatarAdapter.recyclerView.findViewHolderForAdapterPosition(
                                position
                            ) as HomePageAvatarAdapter.VH
                        val ivBg2 = viewHolder.binding.ivAvatar
                        popupView.updateSrcView(ivBg2)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val binding =
            AdapterHomepageHeadBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }
}
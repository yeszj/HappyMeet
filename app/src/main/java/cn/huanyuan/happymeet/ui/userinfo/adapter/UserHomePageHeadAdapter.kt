package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterHomepageHeadBinding
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.view.IconTagInfoView
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

    override fun onBindViewHolder(holder: VH, item: UserDetailInfo?) {
        holder.binding.apply {
            userinfo = item
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

    private fun AdapterHomepageHeadBinding.bindAvatar(item: UserDetailInfo?) {
        if (item==null){
            return
        }
        var homePageAvatarAdapter = rvAvatar.tag as HomePageAvatarAdapter?
        if (homePageAvatarAdapter == null) {
            homePageAvatarAdapter = HomePageAvatarAdapter()
            homePageAvatarAdapter.setOnItemClickListener(OnItemClickListener { _, _, position ->
                item.coverImg = item.thumbnail[position]
                homePageAvatarAdapter.setSelectPosition(position)
            })

            rvAvatar.adapter = homePageAvatarAdapter
        }
        homePageAvatarAdapter.submitList(item.thumbnail)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val binding =
            AdapterHomepageHeadBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }
}
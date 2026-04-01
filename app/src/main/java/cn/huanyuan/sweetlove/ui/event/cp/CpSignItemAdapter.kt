package cn.huanyuan.sweetlove.ui.event.cp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.HeartCpSignInfo
import cn.huanyuan.sweetlove.databinding.AdapterHeartCpSignItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import coil.load
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class CpSignItemAdapter : BaseQuickAdapter<HeartCpSignInfo, CpSignItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterHeartCpSignItemBinding = AdapterHeartCpSignItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: HeartCpSignInfo?) {
        holder.binding.apply {
            viewBorder.visibility = View.INVISIBLE
            ivBox.load(item?.icon)
            changeSelect(position)
            if (item?.afterBind == false || !isHasCp) {
                //已经过期
                showGrayStyle(position)
                tvSign.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.blackAlpha80))
            } else {
                viewBg.setGradientColor(90,
                    "#FFF8FB".toColorInt(),
                    "#FFF0F7".toColorInt(),
                    "#FFE2EF".toColorInt()
                )
                val result = DateUtils.compareWithToday(item!!.date)
                val signStatus = item.signStatus ///0双方未签 / 1对方已签 / 2我已签 / 3双方已签
                val rewardStatus = item.rewardStatus //0未领取 / 1可领取 / 2已领取
                ivBottom.visibility = View.VISIBLE

                if (result == -1) {
                    tvSign.setTextColor("#FFFFFF".toColorInt())
                    // 在当天之前
                    if (signStatus != 3) {
                        //显示补签
                        tvSign.text = "补签"
                        TextViewDrawableUtils.setDrawableLeft(
                            tvSign,
                            ContextCompat.getDrawable(context, R.drawable.svg_cp_sign)
                        )
                    } else {
                        if (rewardStatus == 2) {
                            tvSign.text = item.rewardDesc
                            TextViewDrawableUtils.setDrawableLeft(tvSign, null)
                        } else {
                            tvSign.text = "双方点亮"
                            TextViewDrawableUtils.setDrawableLeft(
                                tvSign,
                                ContextCompat.getDrawable(context, R.drawable.svg_heart_cp_1)
                            )
                        }
                    }

                } else if (result == 0) {
                    // 当天
                    tvSign.setTextColor("#FFFFFF".toColorInt())
                    if (signStatus == 2 || signStatus == 1) {
                        tvSign.text = "一方点亮"
                        TextViewDrawableUtils.setDrawableLeft(
                            tvSign,
                            ContextCompat.getDrawable(context, R.drawable.svg_heart_cp_love_3)
                        )
                    } else if (signStatus == 3) {
                        if (rewardStatus == 2) {
                            tvSign.text = item.rewardDesc
                            TextViewDrawableUtils.setDrawableLeft(tvSign, null)
                        } else {
                            tvSign.text = "双方点亮"
                            TextViewDrawableUtils.setDrawableLeft(
                                tvSign,
                                ContextCompat.getDrawable(context, R.drawable.svg_heart_cp_1)
                            )
                        }
                    } else if (signStatus == 0) {
                        tvSign.text = "立即点亮"
                        TextViewDrawableUtils.setDrawableLeft(
                            tvSign,
                            ContextCompat.getDrawable(context, R.drawable.svg_heart_cp_love_2)
                        )
                    }

                } else if (result == 1) {
                    // 在当天之后
                    showGrayStyle(position)
                    tvSign.setTextColor("#80FF78AF".toColorInt())
                }
            }
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: HeartCpSignInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun AdapterHeartCpSignItemBinding.showGrayStyle(position: Int) {
        val color = "#0A000000".toColorInt()
        viewBg.setGradientColor(90,color, color, color)
        tvSign.text = "第${position + 1}天"
        TextViewDrawableUtils.setDrawableLeft(tvSign, null)
        ivBottom.visibility = View.INVISIBLE
    }


    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }


    var isHasCp = false
    fun setHasBindCp(isHasCp: Boolean) {
        this.isHasCp = isHasCp
    }

    private fun AdapterHeartCpSignItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            viewBorder.visibility = View.VISIBLE
        } else {
            viewBorder.visibility = View.INVISIBLE
        }
    }

    private var selectPosition: Int = -1
    fun setSelectPosition(position: Int) {
        if (selectPosition != position) {
            val oldPosition = selectPosition
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }

    fun getSelectSignItem(): HeartCpSignInfo? {
        return if (selectPosition >= 0 && selectPosition < itemCount) {
            getItem(selectPosition)
        } else {
            null
        }
    }


}
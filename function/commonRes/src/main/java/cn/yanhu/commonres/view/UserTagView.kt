package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.SexManager

/**
 * @author: witness
 * created: 2022/12/5
 * desc:
 */
class UserTagView : LinearLayout {
    private lateinit var tvTag: AppCompatTextView

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_user_age, this, true)
        tvTag = findViewById(R.id.tv_tag)
    }


    /**
     * 群组异性人数标签
     */
    @SuppressLint("SetTextI18n")
    fun setGroupUserCount(userCount: String) {
        TextViewDrawableUtils.setDrawableLeft(
            tvTag,
            null
        )
        tvTag.text = userCount
        if (SexManager.isMan(AppCacheManager.gender)) {
            tvTag.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FF879D"))
        } else {
            tvTag.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#6DC9BA"))
        }
    }


    /**
     * 群组总人数标签
     */
    @SuppressLint("SetTextI18n")
    fun setGroupUserTotalCount(userCount: Int) {
        TextViewDrawableUtils.setDrawableLeft(
            tvTag,
            null
        )
        tvTag.backgroundTintList =
            ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.groupUserTagColor))
        tvTag.text = "${userCount}人"
    }


    /**
     * 群组在线人数标签
     */
    @SuppressLint("SetTextI18n")
    fun setOnLineCount(count: Int) {
        TextViewDrawableUtils.setDrawableLeft(
            tvTag,
            null
        )
        tvTag.backgroundTintList =
            ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.onlineTagColor))
        tvTag.text = "${count}人在线"
    }


    fun setUserLocation(province: String?) {
        if (TextUtils.isEmpty(province)) {
            tvTag.visibility = View.GONE
        } else {
            tvTag.visibility = View.VISIBLE
        }
        TextViewDrawableUtils.setDrawableLeft(
            tvTag,
            ContextCompat.getDrawable(context, R.drawable.svg_location_white)
        )
        tvTag.textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_11).toFloat()
        tvTag.backgroundTintList =
            ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.locationTagColor))
        tvTag.text = province
    }


    @SuppressLint("SetTextI18n")
    fun setUserAge(age: Int, gender: Int) {
        if (SexManager.isMan(gender)) {
            TextViewDrawableUtils.setDrawableLeft(
                tvTag,
                ContextCompat.getDrawable(context, R.drawable.svg_man)
            )
            tvTag.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.manColor))
        } else {
            TextViewDrawableUtils.setDrawableLeft(
                tvTag,
                ContextCompat.getDrawable(context, R.drawable.svg_female_white)
            )
            tvTag.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        }
        tvTag.text = age.toString()
    }

    /**
     * 群组在线人数标签
     */
    @SuppressLint("SetTextI18n")
    fun setTagValue(value: String,colorId:Int,tagTextColorId:Int) {
        TextViewDrawableUtils.setDrawableLeft(
            tvTag,
            null
        )
        tvTag.setTextColor(tagTextColorId)
        tvTag.backgroundTintList =
            ColorStateList.valueOf(colorId)
        tvTag.text = value
    }

}
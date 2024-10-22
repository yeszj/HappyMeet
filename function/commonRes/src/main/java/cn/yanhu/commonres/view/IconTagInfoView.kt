package cn.yanhu.commonres.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.TagInfo

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class IconTagInfoView : LinearLayout {
    private lateinit var tvTag: AppCompatTextView
    private lateinit var ivIcon:AppCompatImageView
    private lateinit var vgTag:ViewGroup
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
        LayoutInflater.from(context).inflate(R.layout.view_icon_tag, this, true)
        tvTag = findViewById(R.id.tv_tag)
        ivIcon = findViewById(R.id.iv_icon)
        vgTag = findViewById(R.id.vg_tag)
    }

    fun setTagValue(value:String){
        tvTag.text = value
        ivIcon.visibility = View.GONE
        ViewUtils.setViewPadding(vgTag,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8),CommonUtils.getDimension(
            com.zj.dimens.R.dimen.dp_4))
    }

    fun setTagValue(tagInfo:TagInfo){
        tvTag.text = tagInfo.value
        if (TextUtils.isEmpty(tagInfo.icon)){
            ivIcon.visibility = View.GONE
        }
        GlideUtils.load(context,tagInfo.icon,ivIcon,-1)
    }
}
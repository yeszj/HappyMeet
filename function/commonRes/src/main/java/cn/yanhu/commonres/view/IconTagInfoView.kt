package cn.yanhu.commonres.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import cn.yanhu.baselib.utils.GlideUtils
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
    }

    fun setTagValue(tagInfo:TagInfo){
        tvTag.text = tagInfo.value
        GlideUtils.load(context,tagInfo.icon,ivIcon,-1)
    }
}
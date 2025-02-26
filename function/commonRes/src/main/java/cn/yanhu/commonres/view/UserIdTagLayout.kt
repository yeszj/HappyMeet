package cn.yanhu.commonres.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.ViewUserIdTagBinding

/**
 * @author: zhengjun
 * created: 2025/2/14
 * desc:
 */
class UserIdTagLayout : LinearLayout {
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

    private lateinit var mBinding:ViewUserIdTagBinding
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_user_id_tag, this, true
        )
    }

    fun setUserInfo(beautyIdImg: String?,userId:String?){
        mBinding.beautifulIdImg = beautyIdImg
        mBinding.userId = userId
        mBinding.executePendingBindings()
    }

    fun setUserIdTagColor(tagBgColorId:Int,tagTextColor:Int){
        mBinding.tvCopyId.setTagColor(tagBgColorId,tagTextColor)
    }
}
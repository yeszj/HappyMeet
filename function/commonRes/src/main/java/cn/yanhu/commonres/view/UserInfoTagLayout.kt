package cn.yanhu.commonres.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.databinding.ViewUserInfoTagLayoutBinding

/**
 * @author: zhengjun
 * created: 2025/2/14
 * desc:
 */
class UserInfoTagLayout : LinearLayout {
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

    private lateinit var mBinding:ViewUserInfoTagLayoutBinding
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_user_info_tag_layout, this, true
        )
    }

    fun setUserInfo(userInfo: BaseUserInfo){
        mBinding.userinfo = userInfo
        mBinding.executePendingBindings()
    }
}
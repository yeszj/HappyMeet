package cn.yanhu.imchat.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.imchat.databinding.ViewImChatUserInfoBinding
import com.blankj.utilcode.util.GsonUtils
import cn.yanhu.imchat.R
import cn.yanhu.imchat.bean.GroupUserInfo

/**
 * @author: zhengjun
 * created: 2024/1/5
 * desc:
 */
class ImChatUserInfoView : LinearLayout {
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

    private lateinit var mBinding: ViewImChatUserInfoBinding

    @SuppressLint("Recycle")
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_im_chat_user_info, this, true
        )
    }

    fun setUserInfo(user: GroupUserInfo) {
        val fromJson = GsonUtils.fromJson(GsonUtils.toJson(user), BaseUserInfo::class.java)
        mBinding.userInfo = fromJson
        mBinding.tvNickName.text = user.nickName
//        if (user.nobleLevel > 0) {
//            mBinding.tvNickName.setNickNameGradient()
//        } else {
//            mBinding.tvNickName.setNickNameDefault()
//        }
    }

    fun setNickName(nickName: String) {
        mBinding.tvNickName.text = nickName
        //mBinding.tvNickName.setNickNameDefault()
    }

}
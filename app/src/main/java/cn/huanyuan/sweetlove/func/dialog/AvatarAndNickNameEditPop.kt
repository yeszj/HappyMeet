package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.databinding.PopAvatarAndNicknameEditBinding
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.manager.ImageSelectManager

/**
 * @author: zhengjun
 * created: 2024/11/8
 * desc:头像 昵称补全弹框
 */
@SuppressLint("ViewConstructor")
class AvatarAndNickNameEditPop(
    val context: FragmentActivity,
    private val needUploadAvatar: Boolean,
    private val needEditNickName: Boolean,
    private val onEditResultListener: OnEditResultListener
) : CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            context: FragmentActivity,
            needUploadAvatar: Boolean,
            needEditNickName: Boolean,
            onEditResultListener: OnEditResultListener
        ): AvatarAndNickNameEditPop {
            val matchPop =
                AvatarAndNickNameEditPop(
                    context,
                    needUploadAvatar,
                    needEditNickName,
                    onEditResultListener
                )
            val builder = XPopup.Builder(context).maxWidth(ScreenUtils.getScreenWidth())
            builder
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_avatar_and_nickname_edit
    }

    private lateinit var mBiding: PopAvatarAndNicknameEditBinding
    override fun onCreate() {
        super.onCreate()
        mBiding = PopAvatarAndNicknameEditBinding.bind(popupImplView)
        if (needEditNickName) {
            mBiding.etNickName.visibility = View.VISIBLE
        } else {
            mBiding.etNickName.visibility = View.GONE
        }
        if (needUploadAvatar) {
            mBiding.ivAvatar.visibility = View.VISIBLE
            mBiding.tvClick.visibility = View.VISIBLE
        } else {
            mBiding.ivAvatar.visibility = View.GONE
            mBiding.tvClick.visibility = View.GONE
        }
        if (needEditNickName && needUploadAvatar) {
            mBiding.tvTitle.text = "头像、昵称未补全"
        } else if (needUploadAvatar) {
            mBiding.tvTitle.text = "头像未补全"
        } else if (needEditNickName) {
            mBiding.tvTitle.text = "昵称未补全"
        }
        mBiding.ivAvatar.setOnSingleClickListener {
            selectPic()
        }
        isSaveNickSuccess = !needEditNickName
        isSaveAvatarSuccess = !needUploadAvatar
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
        mBiding.etCommit.setOnClickListener {
            if (needUploadAvatar) {
                if (TextUtils.isEmpty(availablePath)) {
                    showToast("请点击上传头像")
                    return@setOnClickListener
                }
            }
            if (needEditNickName) {
                val nickName = mBiding.etNickName.text.toString().trim()
                if (TextUtils.isEmpty(nickName)) {
                    showToast("请输入昵称")
                    return@setOnClickListener
                }
                onEditResultListener.onEditNickName(nickName)
            }
            if (needUploadAvatar) {
                onEditResultListener.onEditAvatar(availablePath)
            }
        }
    }

    private var isSaveNickSuccess = false
    private var isSaveAvatarSuccess = false


    private var availablePath = ""
    private fun selectPic() {
        ImageSelectManager.selectPic(
            context,
            isCrop = true,
            type = ImageSelectUtils.TYPE_IMAGE,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    if (!result.isNullOrEmpty()) {
                        val localMedia = result[0]
                        availablePath = localMedia.availablePath
                        GlideUtils.load(context, availablePath, mBiding.ivAvatar)
                    }
                }

                override fun onCancel() {
                }
            })
    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        KeyboardUtils.hideSoftInput(mBiding.etNickName)
    }

    interface OnEditResultListener {
        fun onEditNickName(nickName: String)
        fun onEditAvatar(availablePath: String)
    }
}
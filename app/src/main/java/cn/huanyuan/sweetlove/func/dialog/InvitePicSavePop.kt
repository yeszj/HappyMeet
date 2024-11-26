package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.lxj.xpopup.core.CenterPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopInvitePicSaveBinding
import cn.yanhu.baselib.callBack.OnSavePictureListener
import cn.yanhu.commonres.utils.BitmapUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/11/26
 * desc:
 */
@SuppressLint("ViewConstructor")
class InvitePicSavePop(
    val context: FragmentActivity,
    private val createQRImage: Bitmap,
    private val shareContent: String,
    private val inviteCode: String
) : CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            createQRImage: Bitmap,
            shareContent: String,
            inviteCode: String
        ): InvitePicSavePop {
            val matchPop = InvitePicSavePop(mContext, createQRImage, shareContent, inviteCode)
            val builder = XPopup.Builder(mContext).enableDrag(false)
                .maxWidth(ScreenUtils.getScreenWidth())
                .maxHeight(ScreenUtils.getScreenHeight())
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_invite_pic_save
    }

    private lateinit var mBinding: PopInvitePicSaveBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopInvitePicSaveBinding.bind(popupImplView)
        mBinding.ivQrCode.setImageBitmap(createQRImage)
        mBinding.tvInviteCode.text = inviteCode
        mBinding.tvContent.text = shareContent
    }

    override fun doAfterShow() {
        super.doAfterShow()
        val view2Bitmap = ImageUtils.view2Bitmap(mBinding.vgContent)
        BitmapUtils.savePicture(context, view2Bitmap, object : OnSavePictureListener {
            override fun onFinish() {
                dismiss()
            }

            override fun onSuccess() {
                dismiss()
            }
        })
    }
}
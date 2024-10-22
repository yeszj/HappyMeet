package cn.huanyuan.sweetlove.ui.invite

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityInviteMainBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.callBack.OnSavePictureListener
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.utils.BitmapUtils
import cn.yanhu.commonres.utils.ZXingUtils
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pcl.sdklib.sdk.share.ContentShare

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:邀请页面
 */
class InviteMainActivity : BaseActivity<ActivityInviteMainBinding, InviteViewModel>(
    R.layout.activity_invite_main, InviteViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        ViewUtils.setViewHeight(
            mBinding.contentBg,
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_400) + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_80)
        )
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getInviteInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.invitePageInfoObservable.observe(this) { it ->
            parseState(it, {
                mBinding.inviteInfo = it
                GlideUtils.loadAsDrawable(mContext, it.inviteBg, object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable, transition: Transition<in Drawable>?
                    ) {
                        mBinding.contentBg.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
                val dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_128)
                val createQRImage = ZXingUtils.createQRImage(it.inviteUrl, dimension, dimension)
                mBinding.ivQrCode.setImageBitmap(createQRImage)
            })
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.vgSave.setOnSingleClickListener {
            val view2Bitmap = ImageUtils.view2Bitmap(mBinding.vgContent)
            BitmapUtils.savePicture(mContext, view2Bitmap, object : OnSavePictureListener {
                override fun onFinish() {
                }

                override fun onSuccess() {
                }
            })
        }
        mBinding.vgShare.setOnSingleClickListener {
            startWxShare()
        }
    }

    private fun startWxShare() {
        val inviteUrl = mBinding.inviteInfo!!.inviteUrl
        val contentShare = ContentShare(mContext)
        contentShare.setShareContent(
            "给你介绍一个对象，快速脱单！", "点开链接，同城交友，告别单身！", inviteUrl
        )
        contentShare.shareToWeiXin()
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, InviteMainActivity::class.java))
        }
    }
}
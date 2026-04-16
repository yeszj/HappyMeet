package cn.huanyuan.sweetlove.ui.userinfo.auth
import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import cn.huanyuan.sweetlove.databinding.ActivityUploadIdCardPicBinding
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import java.util.ArrayList
import androidx.core.net.toUri
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.ThreadUtils

/**
 * @author: zhengjun
 * created: 2026/4/10
 * desc:
 */
class UploadIdCardPicActivity : BaseActivity<ActivityUploadIdCardPicBinding, UserViewModel>(
    R.layout.activity_upload_id_card_pic,
    UserViewModel::class.java
) {
    private var frontUrl: String = ""
    private var reverseUrl: String = ""
    private var isCheck: Boolean = false
    override fun initData() {
        setStatusBarStyle(false)
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivIdCardFront.setOnSingleClickListener {
            showSelectPic(true)
        }
        mBinding.ivIdCardReverse.setOnSingleClickListener {
            showSelectPic(false)
        }
        mBinding.tvFront.setOnSingleClickListener {
            if (!CommonUtils.isEmpty(frontUrl)) {
                showSelectPic(true)
            }
        }
        mBinding.tvReverse.setOnSingleClickListener {
            if (!CommonUtils.isEmpty(reverseUrl)) {
                showSelectPic(false)
            }
        }
        mBinding.tvCommit.setOnSingleClickListener {
            if (CommonUtils.isEmpty(frontUrl)) {
                showToast("请上传身份证正面图片")
            } else if (CommonUtils.isEmpty(reverseUrl)) {
                showToast("请上传身份证反面图片")
            } else if (!isCheck) {
                showToast("请勾选同意授权")
            }
        }
        mBinding.tvAgreement.setOnClickListener {
            isCheck = !isCheck
            if (isCheck) {
                TextViewDrawableUtils.setDrawableLeft(
                    mBinding.tvAgreement, ContextCompat.getDrawable(
                        mContext,
                        cn.yanhu.commonres.R.drawable.svg_selected_r20
                    )
                )
            } else {
                TextViewDrawableUtils.setDrawableLeft(
                    mBinding.tvAgreement, ContextCompat.getDrawable(
                        mContext,
                        cn.yanhu.commonres.R.drawable.svg_unselected_r20
                    )
                )
            }
        }
    }

    private fun showSelectPic(isFront: Boolean) {
        ImageSelectManager.selectPic(
            mContext,
            true,
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_336),
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_180),
            1,
            object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    val availablePath = result?.get(0)?.availablePath ?: return
                    if (isFront) {
                        frontUrl = ""
                        mBinding.ivIdCardFront.setImageURI(availablePath.toUri())
                        mBinding.tvFront.text = "重新上传"
                        mBinding.tvFront.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.blackAlpha80))
                    } else {
                        reverseUrl = ""
                        mBinding.ivIdCardReverse.setImageURI(availablePath.toUri())
                        mBinding.tvReverse.text = "重新上传"
                        mBinding.tvReverse.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.blackAlpha80))
                    }
                    uploadPic(isFront, availablePath)
                }

                override fun onCancel() {
                }
            })
    }

    private fun uploadPic(isFront: Boolean, picPath: String) {
        UploadFileClient.uploadFile(
            picPath,
            object : UploadFileProgressListener {
                @SuppressLint("SetTextI18n")
                override fun onProgress(hasWrittenLen: Long, totalLen: Long) {
                    ThreadUtils.getMainHandler().post {
                        var progress = (hasWrittenLen * 100 / totalLen).toInt()
                        if (progress == 100) {
                            progress = 99
                        }
                        if (isFront) {
                            mBinding.tvFrontProgress.visibility = View.VISIBLE
                            mBinding.tvFrontProgress.text = "${progress}%"
                        } else {
                            mBinding.tvVerseProgress.visibility = View.VISIBLE
                            mBinding.tvVerseProgress.text = "${progress}%"
                        }
                    }
                }

                override fun onUploadSuccess(url: String) {
                    if (isFront) {
                        frontUrl = url
                        mBinding.tvFrontProgress.visibility = View.INVISIBLE
                    } else {
                        reverseUrl = url
                        mBinding.tvVerseProgress.visibility = View.INVISIBLE
                    }
                }
            }
        )
    }
}
package cn.huanyuan.happymeet.ui.login.profile

import android.annotation.SuppressLint
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgEditAvatarAndNameBinding
import cn.huanyuan.happymeet.ui.login.LoginViewModel
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.SexManager
import cn.yanhu.commonres.pop.UploadAvatarPop
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.ThreadUtils
import java.util.Random

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
class EditAvatarAndNameFrg : BaseFragment<FrgEditAvatarAndNameBinding, LoginViewModel>(
    R.layout.frg_edit_avatar_and_name,
    LoginViewModel::class.java
) {
    private lateinit var selfViewModel: LoginViewModel

    @SuppressLint("SetTextI18n")
    override fun initData() {
        selfViewModel = (context as CompleteProfileActivity).mViewModel
        mBinding.viewModel = selfViewModel
        mBinding.tvTips2.text = "上传本人清晰头像有助于脱单哦\uD83D\uDE09"
        setRandomNickName()
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivAvatar.setOnSingleClickListener {
            UploadAvatarPop.showDialog(mContext,SexManager.isMan(selfViewModel.personInfo.value!!.gender),object : UploadAvatarPop.OnSelectPicResultListener{
                override fun onPicPath(availablePath: String) {
                    mBinding.ivAvatar.isOval = true
                    GlideUtils.load(mContext, availablePath, mBinding.ivAvatar)
                    uploadAvatar(availablePath)
                }
            })
        }
        mBinding.tvSwitch.setOnSingleClickListener {
            setRandomNickName()
        }
    }

    private fun setRandomNickName() {
        val personInfo = selfViewModel.personInfo.value
        if (SexManager.isMan(personInfo!!.gender)) {
            val maleNickNames = selfViewModel.loginSuccessInfo.value!!.maleNickNames
            if (!maleNickNames.isNullOrEmpty()) {
                personInfo.nickName =
                    maleNickNames[Random().nextInt(maleNickNames.size) % (maleNickNames.size + 1)]
            }
        } else {
            val womanNameList = selfViewModel.loginSuccessInfo.value!!.femaleNickNames
            if (!womanNameList.isNullOrEmpty()) {
                personInfo.nickName =
                    womanNameList[Random().nextInt(womanNameList.size) % (womanNameList.size + 1)]
            }
        }
    }

    private fun uploadAvatar(availablePath: String) {
        mBinding.tvProgress.visibility = View.VISIBLE
        UploadFileClient.uploadFile(
            availablePath,
            object : UploadFileProgressListener {
                @SuppressLint("SetTextI18n")
                override fun onProgress(hasWrittenLen: Long, totalLen: Long) {
                    ThreadUtils.getMainHandler().post {
                        var progress = (hasWrittenLen * 100 / totalLen).toInt()
                        if (progress == 100) {
                            progress = 99
                        }
                        mBinding.tvProgress.text = "${progress}%"

                    }
                }

                override fun onUploadSuccess(url: String) {
                    ThreadUtils.getMainHandler().post {
                        selfViewModel.personInfo.value?.portrait = url
                        mBinding.tvProgress.visibility = View.INVISIBLE
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    mBinding.ivAvatar.isOval = false
                    mBinding.ivAvatar.setImageResource(cn.yanhu.commonres.R.drawable.icon_click_avatar)
                    mBinding.tvProgress.visibility = View.INVISIBLE
                }
            })
    }

    companion object {
        fun newsInstance(): EditAvatarAndNameFrg {
            return EditAvatarAndNameFrg()
        }
    }
}
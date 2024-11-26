package cn.huanyuan.sweetlove.ui.invite

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityInviteMainBinding
import cn.huanyuan.sweetlove.func.dialog.InvitePicSavePop
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.utils.ZXingUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.pcl.sdklib.sdk.share.ContentShare

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:邀请页面
 */
class InviteMainActivity : BaseActivity<ActivityInviteMainBinding, InviteViewModel>(
    R.layout.activity_invite_main, InviteViewModel::class.java
) {
    private var contentList: MutableList<String> = mutableListOf()
    private var createQRImage: Bitmap? = null
    private var inviteContent: String = ""
    override fun initData() {
        setStatusBarStyle(false)
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getInviteInfo()
        request({rxApi.getInviteContents() },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                val data1 = data.data
                contentList = GsonUtils.fromJson(data1,
                    object : TypeToken<List<String>>() {}.type)
                setRandomContent()
            }

        })
    }

    private fun setRandomContent() {
        inviteContent = contentList.random()
        mBinding.tvInviteCotent.text = inviteContent
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.invitePageInfoObservable.observe(this) { it ->
            parseState(it, {
                mBinding.inviteInfo = it
                val dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_128)
                createQRImage = ZXingUtils.createQRImage(it.inviteUrl, dimension, dimension)
                mBinding.ivQrCode.setImageBitmap(createQRImage)
            })
        }
    }

    private var invitePicSavePop:InvitePicSavePop?=null
    override fun initListener() {
        super.initListener()
        mBinding.vgSave.setOnSingleClickListener {
            createQRImage?.apply {
                DialogUtils.showLoading("正在保存...")
                showSavePop()
            }
        }
        mBinding.vgShare.setOnSingleClickListener {
            startWxShare()
        }
        mBinding.ivSwitch.setOnClickListener {
            setRandomContent()
        }
        mBinding.ivCopy.setOnSingleClickListener{
            mBinding.inviteInfo?.apply {
                ClipboardUtils.copyText(mBinding.inviteInfo!!.userId)
                showToast("复制成功")
            }

        }
    }

    private fun showSavePop() {
        if (CommonUtils.isPopShow(invitePicSavePop)) {
            return
        }
        invitePicSavePop = InvitePicSavePop.showDialog(
            mContext,
            createQRImage!!,
            inviteContent,
            mBinding.inviteInfo!!.userId
        )
    }

    private fun startWxShare() {
        val inviteUrl = mBinding.inviteInfo!!.inviteUrl
        val contentShare = ContentShare(mContext)
        contentShare.setShareContent(
            "给你介绍一个对象，快速脱单！", inviteContent, inviteUrl
        )
        contentShare.shareToWeiXin()
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, InviteMainActivity::class.java))
        }
    }
}
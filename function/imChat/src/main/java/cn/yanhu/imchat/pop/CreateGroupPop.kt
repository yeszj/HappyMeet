package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.ImageSelectManager
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.imchat.R
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.databinding.PopCreateGroupBinding
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.ThreadUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import java.util.ArrayList

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
@SuppressLint("ViewConstructor")
class CreateGroupPop(val context: FragmentActivity, val groupInfo: GroupDetailInfo? = null) :
    BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_create_group
    }

    private lateinit var mBinding: PopCreateGroupBinding
    private var groupIconUrl: String = ImUserManager.getSelfUserInfo()!!.portrait
    override fun onCreate() {
        super.onCreate()
        mBinding = PopCreateGroupBinding.bind(popupImplView)
        mBinding.apply {
            if (groupInfo == null) {
                GlideUtils.load(context, groupIconUrl, mBinding.ivGroupIcon)
                mBinding.btnConfirm.text = "确认创建"
            } else {
                mBinding.etGroupName.setText(groupInfo.groupName)
                mBinding.etGroupDesc.setText(groupInfo.groupNotice)
                groupIconUrl = groupInfo.groupIcon
                GlideUtils.load(context, groupIconUrl, mBinding.ivGroupIcon)
                mBinding.btnConfirm.text = "保存"
            }
            bgAvatar.setOnSingleClickListener {
                ImageSelectManager.selectPic(
                    context,
                    true,
                    type = ImageSelectUtils.TYPE_IMAGE,
                    call = object : OnResultCallbackListener<LocalMedia> {
                        override fun onResult(result: ArrayList<LocalMedia>?) {
                            if (!result.isNullOrEmpty()) {
                                val localMedia = result[0]
                                val availablePath = localMedia.availablePath
                                GlideUtils.load(context, availablePath, ivGroupIcon)
                                uploadAvatar(availablePath)
                            }
                        }
                        override fun onCancel() {
                        }
                    })
            }
            mBinding.btnConfirm.setOnSingleClickListener {
                createGroup()
            }
            mBinding.ivClose.setOnSingleClickListener { dismiss() }
        }
    }

    private fun createGroup() {
        val map = mutableMapOf<String,String>()
        if (groupInfo!=null){
            map["groupId"] = groupInfo.groupId
        }
        val groupName = mBinding.etGroupName.text.toString().trim()
        if (TextUtils.isEmpty(groupName)) {
            showToast("请输入群名称")
            return
        }
        map["groupName"] = groupName
        if (TextUtils.isEmpty(groupIconUrl)) {
            showToast("请上传群头像")
            return
        }
        map["groupIcon"] = groupIconUrl
        val groupDesc = mBinding.etGroupDesc.text.toString().trim()
        if (TextUtils.isEmpty(groupDesc)) {
            showToast("请输入群介绍")
            return
        }
        map["groupNotice"] = groupDesc

        DialogUtils.showLoading()
        request({ imChatRxApi.createGroup(map)},object : OnBooleanResultListener{
            override fun onSuccess() {
                dismiss()
                if (groupInfo!=null){
                    showToast("修改成功")
                }else{
                    showToast("创建成功")
                }
                DialogUtils.dismissLoading()
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })

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
                        groupIconUrl = url
                        mBinding.tvProgress.visibility = View.INVISIBLE
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    mBinding.tvProgress.visibility = View.INVISIBLE
                }
            })
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            groupInfo: GroupDetailInfo? = null
        ): CreateGroupPop {
            val createGroupPop = CreateGroupPop(mContext, groupInfo)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(createGroupPop).show()
            return createGroupPop
        }
    }
}
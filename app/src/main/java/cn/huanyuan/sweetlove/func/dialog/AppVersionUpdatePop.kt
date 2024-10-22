package cn.huanyuan.sweetlove.func.dialog

import android.Manifest
import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.databinding.PopAppVersionUpdateBinding
import cn.huanyuan.sweetlove.func.manager.ApkDownloadManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.utils.PermissionXUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

/**
 * @author: zhengjun
 * created: 2024/10/12
 * desc:
 */
@SuppressLint("ViewConstructor")
class AppVersionUpdatePop(val context: FragmentActivity, val appVersionInfo: AppVersionInfo) :
    CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_app_version_update
    }

    private lateinit var mBinding: PopAppVersionUpdateBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopAppVersionUpdateBinding.bind(popupImplView)
        val split: List<String> = appVersionInfo.content.split(",")
        val stringBuilder = StringBuilder()
        for (i in split.indices) {
            stringBuilder.append("  •  " + split[i])
            if (i + 1 != split.size) {
                stringBuilder.append("\n")
            }
        }
        appVersionInfo.content = stringBuilder.toString()
        mBinding.versionInfo = appVersionInfo
        mBinding.executePendingBindings()
        mBinding.progress.setOnSingleClickListener {
            isUpDataApkPermission()
        }
    }

    private fun isUpDataApkPermission() {
        //判断是否授权必要权限
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        PermissionXUtils.checkInstallPermission(context,
            permissions,
            "为爱交友想访问您以下权限，用于APP的版本更新",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    mBinding.progress.progress = 0
                    mBinding.tvProgress.text = "下载中 0%"
                    ApkDownloadManager.sharedInstance().downloadApk(context,
                        appVersionInfo,
                        object : ApkDownloadManager.OnProgressListener {
                            @SuppressLint("SetTextI18n")
                            override fun onProgress(progress: Int) {
                                mBinding.progress.progress = progress
                                mBinding.tvProgress.text = "下载中 ${progress}%"
                            }

                            override fun onFinish() {
                                dismiss()
                            }

                        })
                }

                override fun onFail() {
                }

            })
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            item: AppVersionInfo,
        ): AppVersionUpdatePop {
            val matchPop = AppVersionUpdatePop(mContext, item)
            val builder = XPopup.Builder(mContext)
            builder
                .dismissOnTouchOutside(item.forceUpdates!=1)
                .dismissOnBackPressed(item.forceUpdates!=1)
                .asCustom(matchPop).show()
            return matchPop
        }
    }

}
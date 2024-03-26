package cn.yanhu.commonres.utils

import android.Manifest
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.pop.CommonPermissionPop
import cn.yanhu.commonres.pop.CommonPermissionPop.Companion.showDialog
import cn.yanhu.commonres.pop.SystemAlertPermissionDialog
import com.blankj.utilcode.util.ActivityUtils
import com.hjq.toast.ToastUtils
import com.permissionx.guolindev.PermissionMediator
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.ForwardToSettingsCallback
import com.permissionx.guolindev.callback.RequestCallback
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope

object PermissionXUtils {
    fun checkPermission(
        fragmentActivity: FragmentActivity,
        permissions: ArrayList<String>,
        explainReasonStr: String,
        failedStr: String?,
        permissionListener: PermissionListener
    ) {
        val permissionMediator = PermissionX.init(fragmentActivity)
        isRequestPermission(
            permissionMediator,
            permissions,
            explainReasonStr,
            failedStr,
            permissionListener
        )
    }

    fun checkPermission(
        fragment: Fragment,
        permissions: ArrayList<String>,
        explainReasonStr: String,
        failedStr: String?,
        permissionListener: PermissionListener
    ) {
        val permissionMediator = PermissionX.init(fragment)
        isRequestPermission(
            permissionMediator,
            permissions,
            explainReasonStr,
            failedStr,
            permissionListener
        )
    }

    fun hasPermission(permissions: ArrayList<String>): Boolean {
        for (permission in permissions) {
            if (permission == Manifest.permission.REQUEST_INSTALL_PACKAGES) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!ActivityUtils.getTopActivity().packageManager.canRequestPackageInstalls()) {
                        return true
                    }
                }
            } else if (permission == PermissionX.permission.POST_NOTIFICATIONS) {
                if (!PermissionX.areNotificationsEnabled(ActivityUtils.getTopActivity())) {
                    return true
                }
            } else {
                if (!PermissionX.isGranted(ActivityUtils.getTopActivity(), permission)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isRequestPermission(
        permissionMediator: PermissionMediator,
        permissions: ArrayList<String>,
        explainReasonStr: String,
        failedStr: String?,
        permissionListener: PermissionListener
    ) {
        val commonPermissionPop: CommonPermissionPop? = if (hasPermission(permissions)) {
            showDialog((ActivityUtils.getTopActivity() as FragmentActivity), explainReasonStr)
        } else {
            null
        }
        permissionMediator
            .permissions(permissions)
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您未授权相关权限，可在应用程序设置当中手动开启权限",
                    "好的",
                    "取消"
                )
            }
            .request { allGranted: Boolean, _: List<String?>?, _: List<String?>? ->
                if (commonPermissionPop != null && commonPermissionPop.isShow) {
                    commonPermissionPop.dismiss()
                }
                if (allGranted) {
                    permissionListener.onSuccess()
                } else {
                    permissionListener.onFail()
                    ToastUtils.show(if (failedStr.isNullOrEmpty()) "您拒绝授权权限，将无法体验部分功能" else failedStr)
                }
            }
    }

    fun checkAlertPermission(
        fragmentActivity: FragmentActivity?,
        permissionListener: OnAlertPermissionListener
    ) {
        PermissionX.init(fragmentActivity!!)
            .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope: ExplainScope, _: List<String?>? ->
                val systemAlertPermissionDialog = SystemAlertPermissionDialog(
                    fragmentActivity, object : SystemAlertPermissionDialog.OnClickCloseListener {
                       override fun onClose() {
                            permissionListener.onClose()
                        }
                    })
                scope.showRequestReasonDialog(systemAlertPermissionDialog)
            }
            .onForwardToSettings { scope: ForwardScope, deniedList: List<String> ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您未授权相关权限，可在应用程序设置当中手动开启权限",
                    "好的",
                    "取消"
                )
            }
            .request { allGranted: Boolean, _: List<String>, _: List<String> ->
                if (allGranted) {
                    permissionListener.onSuccess()
                } else {
                    permissionListener.onFail()
                    ToastUtils.show("您拒绝授权权限，将无法体验部分功能")
                }
            }
    }

    fun checkInstallPermission(
        fragmentActivity: FragmentActivity,
        permissions: List<String>,
        tips: String,
        permissionListener: PermissionListener
    ) {
        PermissionX.init(fragmentActivity)
            .permissions(permissions)
            .explainReasonBeforeRequest()
            .onExplainRequestReason(ExplainReasonCallback { scope, _ ->
                scope.showRequestReasonDialog(
                    permissions,
                    tips,
                    "去设置"
                )
            })
            .onForwardToSettings(ForwardToSettingsCallback { scope: ForwardScope, deniedList: List<String> ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您未授权相关权限，可在应用程序设置当中手动开启权限",
                    "好的",
                    "取消"
                )
            })
            .request(RequestCallback { allGranted: Boolean, _: List<String?>?, _: List<String?>? ->
                if (allGranted) {
                    permissionListener.onSuccess()
                } else {
                    permissionListener.onFail()
                    ToastUtils.show("您拒绝授权权限，将无法体验部分功能")
                }
            })
    }

    interface PermissionListener {
        fun onSuccess()
        fun onFail()
    }

    interface OnAlertPermissionListener {
        fun onSuccess()
        fun onFail()
        fun onClose()
    }
}
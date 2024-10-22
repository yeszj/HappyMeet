package cn.yanhu.agora.manager

import android.Manifest
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.utils.PermissionXUtils

/**
 * @author: zhengjun
 * created: 2023/12/1
 * desc:
 */
object PermissionManager {

    @JvmStatic
    fun checkVideoPermission(context:FragmentActivity,permissionListener: PermissionXUtils.PermissionListener){
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            context,
            permissions,
            "为了便于使用上麦、实名认证等服务，请先同意麦克风、声音、摄像头权限",
            "您拒绝授权相关权限，无法使用部分功能",
            permissionListener)
    }

    fun checkVoicePermission(context:FragmentActivity,permissionListener:PermissionXUtils.PermissionListener){
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            context,
            permissions,
            "对爱交友想访问您的麦克风权限，用于语音通话",
            "您拒绝授权麦克风权限，无法使用部分功能",
            permissionListener)
    }
}
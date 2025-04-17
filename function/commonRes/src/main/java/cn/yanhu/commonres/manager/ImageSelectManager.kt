package cn.yanhu.commonres.manager

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.OperateInfo
import cn.yanhu.commonres.pop.CommonOperatePop
import cn.yanhu.commonres.utils.PermissionXUtils
import com.blankj.utilcode.util.AppUtils
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

/**
 * @author: zhengjun
 * created: 2024/3/1
 * desc:
 */
object ImageSelectManager {

    fun selectPic(
        mContext: FragmentActivity,
        isCrop: Boolean = true,
        width: Int = 180,
        height: Int = 180,
        type:Int,
        maxSelectNum:Int = 1,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        checkPermission(mContext,isCrop,width,height,type,maxSelectNum,call)

    }

    fun selectPic(
        mContext: FragmentActivity,
        isCrop: Boolean = true,
        width: Int = 180,
        height: Int = 180,
        maxSelectNum:Int = 1,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val list = mutableListOf<OperateInfo>()
        list.add(OperateInfo("拍照", R.color.cl_common, 1))
        list.add(OperateInfo("打开相册", R.color.colorTextRed, 2))
        CommonOperatePop.showDialog(
            mContext,
            list,
            object : CommonOperatePop.OnClickItemListener {
                override fun onClickItem(operateInfo: OperateInfo) {
                    val operateType =  if (operateInfo.type == 1) {
                        ImageSelectUtils.TYPE_CAMERA
                    } else {
                        ImageSelectUtils.TYPE_IMAGE
                    }
                    checkPermission(mContext,isCrop,width,height,operateType,maxSelectNum,call)

                }
            })

    }

    private fun checkPermission( mContext: FragmentActivity,
                                 isCrop: Boolean = true,
                                 width: Int = 180,
                                 height: Int = 180,
                                 type:Int,
                                 maxSelectNum:Int,
                                 call: OnResultCallbackListener<LocalMedia>) {
        val permissions = ArrayList<String>()
        if (type == ImageSelectUtils.TYPE_CAMERA || type == ImageSelectUtils.TYPE_CAMERA_VIDEO ) {
            permissions.add(Manifest.permission.CAMERA)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when (type) {
                    ImageSelectUtils.TYPE_IMAGE -> {
                        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    }

                    ImageSelectUtils.TYPE_VIDEO -> {
                        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    }

                    else -> {
                        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                }
            } else {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        PermissionXUtils.checkPermission(mContext,permissions,"${AppUtils.getAppName()}想访问您的以下权限，用于图片上传", "您拒绝授权权限，将无法体验部分功能",object :PermissionXUtils.PermissionListener{
            override fun onSuccess() {
                when (type) {
                    ImageSelectUtils.TYPE_IMAGE -> {
                        ImageSelectUtils.selectPic(mContext,isCrop,width,height,maxSelectNum,call)
                    }
                    ImageSelectUtils.TYPE_VIDEO -> {
                        ImageSelectUtils.selectVideo(mContext,isCrop,width,height,maxSelectNum,call)
                    }
                    ImageSelectUtils.TYPE_CAMERA ->{
                        ImageSelectUtils.openCamara(mContext,isCrop,type = SelectMimeType.ofImage(),call=call)
                    }
                    ImageSelectUtils.TYPE_CAMERA_VIDEO ->{
                        ImageSelectUtils.openCamara(mContext,isCrop,type = SelectMimeType.ofVideo(),call)
                    }
                    else -> {
                        ImageSelectUtils.selectVideoOrImage(mContext,isCrop,maxSelectNum,width,height,call = call)
                    }
                }

            }
            override fun onFail() {
            }
        })
    }
}
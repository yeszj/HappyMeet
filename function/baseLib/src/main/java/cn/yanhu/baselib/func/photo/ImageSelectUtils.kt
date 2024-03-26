package cn.yanhu.baselib.func.photo

import android.app.Activity
import android.content.Context
import com.luck.picture.lib.basic.PictureSelectionCameraModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.SandboxTransformUtils


/**
 * @author: witness
 * created: 2022/4/27
 * desc:
 */
object ImageSelectUtils {
    const val TYPE_CAMERA = 1
    const val TYPE_IMAGE = 2
    const val TYPE_VIDEO = 3
    const val TYPE_ALL = 4


    fun selectlePic(
        mContext: Activity,
        isCrop: Boolean = true,
        width: Int = 180,
        height: Int = 180,
        maxSelectNum: Int,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val cropEngine = PictureSelector.create(mContext)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setSandboxFileEngine(MeSandboxFileEngine())
            .setSelectionMode(if (maxSelectNum==1) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(maxSelectNum)
            .setCompressEngine(ImageCompressEngine())
            .isGif(false)
        if (isCrop) {
            cropEngine.setCropEngine(ImageFileCropEngine(width, height))
        }
        cropEngine.forResult(call)
    }



    fun selectSingleVideoOrPic(
        mContext: Activity,
        isCrop: Boolean = true,
        width: Int = 180,
        height: Int = 180,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val cropEngine = PictureSelector.create(mContext)
            .openGallery(SelectMimeType.ofAll())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setSandboxFileEngine(MeSandboxFileEngine())
            .setMaxSelectNum(1)
            .setFilterVideoMaxSecond(30)
            .setCompressEngine(ImageCompressEngine())
            .isGif(false)
        if (isCrop) {
            cropEngine.setCropEngine(ImageFileCropEngine( width, height))
        }
        cropEngine.forResult(call)
    }

    fun selectVideo(
        mContext: Activity,
        isCrop: Boolean = true,
        width: Int = 180,
        height: Int = 180,
        maxSelectNum: Int = 1,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val cropEngine = PictureSelector.create(mContext)
            .openGallery(SelectMimeType.ofVideo())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setSandboxFileEngine(MeSandboxFileEngine())
            .setMaxSelectNum(maxSelectNum)
            .setSelectionMode(if (maxSelectNum==1) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
            .setFilterVideoMaxSecond(30)
            .setCompressEngine(ImageCompressEngine())
            .isGif(false)
        if (isCrop) {
            cropEngine.setCropEngine(ImageFileCropEngine(width, height))
        }
        cropEngine.forResult(call)
    }

    /**
     * isWithSelectVideoImage 图片和视频是否能一起选中
     */
    fun selectVideoOrImage(
        mContext: Activity,
        isCrop: Boolean = true,
        maxSelectNum: Int,
        width: Int = 180,
        height: Int = 18,
        isWithSelectVideoImage: Boolean = false,
        selectType: Int = SelectMimeType.ofAll(),
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val cropEngine = PictureSelector.create(mContext)
            .openGallery(selectType)
            .setImageEngine(GlideEngine.createGlideEngine())
            .setSandboxFileEngine(MeSandboxFileEngine())
            .setMaxSelectNum(maxSelectNum)
            .setSelectionMode(if (maxSelectNum==1) SelectModeConfig.SINGLE else SelectModeConfig.MULTIPLE)
            .setMaxVideoSelectNum(if (!isWithSelectVideoImage) 1 else maxSelectNum)
            .setFilterVideoMaxSecond(30)
            .isWithSelectVideoImage(isWithSelectVideoImage)
            .setCompressEngine(ImageCompressEngine())
            .isGif(true)
        if (isCrop) {
            cropEngine.setCropEngine(ImageFileCropEngine(width, height))
        }
        cropEngine.forResult(call)
    }

    fun openCamara(
        mContext: Activity,
        isCrop: Boolean = true,
        call: OnResultCallbackListener<LocalMedia>
    ) {
        val pictureSelectionCameraModel: PictureSelectionCameraModel =
            PictureSelector.create(mContext).openCamera(SelectMimeType.ofImage())
                .setSandboxFileEngine(MeSandboxFileEngine())
        if (isCrop) {
            pictureSelectionCameraModel.setCropEngine(ImageFileCropEngine())
        }
        pictureSelectionCameraModel.setCompressEngine(ImageCompressEngine())
        pictureSelectionCameraModel.forResult(call)
    }

    /**
     * 自定义沙盒文件处理
     */
    private class MeSandboxFileEngine : UriToFileTransformEngine {
        override fun onUriToFileAsyncTransform(
            context: Context,
            srcPath: String,
            mineType: String,
            call: OnKeyValueResultCallbackListener?,
        ) {
            call?.onCallback(
                srcPath,
                SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
            )
        }
    }
}
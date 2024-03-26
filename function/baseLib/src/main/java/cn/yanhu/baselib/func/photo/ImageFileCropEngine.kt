package cn.yanhu.baselib.func.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ActivityUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luck.picture.lib.engine.CropFileEngine
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import java.io.File

/**
 * @author: witness
 * created: 2022/4/27
 * desc:
 */
class ImageFileCropEngine(
    var width: Int = 180,
    var height: Int = 180
) : CropFileEngine {
    override fun onStartCrop(
        fragment: Fragment,
        srcUri: Uri,
        destinationUri: Uri,
        dataSource: ArrayList<String>,
        requestCode: Int
    ) {
        val options = buildOptions()
        val uCrop = UCrop.of(srcUri, destinationUri, dataSource)

        uCrop.withOptions(options)
        uCrop.setImageEngine(object : UCropImageEngine {
            override fun loadImage(context: Context, url: String, imageView: ImageView) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return
                }
                Glide.with(context).load(url).override(width, height).into(imageView)
            }
            override fun loadImage(
                context: Context,
                url: Uri,
                maxWidth: Int,
                maxHeight: Int,
                call: UCropImageEngine.OnCallbackListener<Bitmap>
            ) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return
                }
                Glide.with(context).asBitmap().override(maxWidth, maxHeight).load(url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            call.onCall(resource)
                        }
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            call.onCall(null)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        })
        uCrop.start(fragment.requireActivity(), fragment, requestCode)
    }

    private fun buildOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(false)
        options.setShowCropFrame(true)
        options.setShowCropGrid(true)
        if (width==height){
            options.withAspectRatio(1f, 1f)
        }else{
            val x = width/100
            val y = height/100
            options.withAspectRatio(x.toFloat(),y.toFloat())
        }
        options.setCircleDimmedLayer(false)
        options.setCropOutputPathDir(sandboxPath)
        options.isCropDragSmoothToCenter(false)
        options.isUseCustomLoaderBitmap(false)
        options.isForbidCropGifWebp(true)
        options.isForbidSkipMultipleCrop(false)
        options.setMaxScaleMultiplier(100f)
        options.setHideBottomControls(true)
        return options
    }

    private val sandboxPath: String
        get() {
            val externalFilesDir = ActivityUtils.getTopActivity().getExternalFilesDir("")
            val customFile = File(externalFilesDir!!.absolutePath, "Sandbox")
            if (!customFile.exists()) {
                customFile.mkdirs()
            }
            return customFile.absolutePath + File.separator
        }
}
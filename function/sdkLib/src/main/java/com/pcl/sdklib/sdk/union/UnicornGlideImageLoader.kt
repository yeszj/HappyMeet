package com.pcl.sdklib.sdk.union

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.qiyukf.unicorn.api.ImageLoaderListener
import com.qiyukf.unicorn.api.UnicornImageLoader

class UnicornGlideImageLoader(context: Context) : UnicornImageLoader {
    private val context: Context

    init {
        this.context = context.applicationContext
    }

    override fun loadImageSync(uri: String, width: Int, height: Int): Bitmap? {
        return null
    }

    override fun loadImage(uri: String, width: Int, height: Int, listener: ImageLoaderListener?) {
        var realWidth = width
        var realHeight = height
        if (width <= 0 || height <= 0) {
            realHeight = Int.MIN_VALUE
            realWidth = height
        }
        Glide.with(context).asBitmap().load(uri)
            .into(object : CustomTarget<Bitmap?>(realWidth, realHeight) {
                override fun onLoadStarted(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    listener?.onLoadComplete(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}
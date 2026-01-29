package cn.yanhu.baselib.func.photo

import android.content.Context
import android.widget.ImageView
import cn.yanhu.baselib.R
import coil.load
import coil.transform.RoundedCornersTransformation
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper


/**
 * @author: witness
 * created: 2022/4/27
 * desc:
 */
class CoilEngine private constructor() : ImageEngine {
    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    override fun loadImage(context: Context, url: String?, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.load(url)
    }

    override fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String?,
        maxWidth: Int,
        maxHeight: Int
    ) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.load(url){
            this.size(maxWidth, maxHeight)
        }
    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadAlbumCover(context: Context, url: String?, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.load(url){
            size(180, 180)
            placeholder(R.drawable.image_placeholder)
            transformations(RoundedCornersTransformation(8f))
        }
    }


    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadGridImage(context: Context, url: String?, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.load(url){
            size(200, 200)
            placeholder(R.drawable.image_placeholder)
        }
    }

    override fun pauseRequests(context: Context) {
    }

    override fun resumeRequests(context: Context) {
    }

    private object InstanceHolder {
        val instance: CoilEngine = CoilEngine()
    }

    companion object {
        fun createCoilEngine(): CoilEngine {
            return InstanceHolder.instance
        }
    }
}

package com.pcl.sdklib.sdk.share

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import cn.yanhu.baselib.utils.ext.showToast
import com.umeng.socialize.ShareAction
import com.umeng.socialize.ShareContent
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb

/**
 * @author zhengjun
 * @desc 配置分享内容及分享平台
 * @created at 2018/8/24 10:18
 */
class ContentShare(private val context: Context) {
    private var content: ShareContent? = null //分享内容对象

    /**
     * @desc 设置分享图片
     * @author zhengjun
     * @created at 2018/8/30 14:34
     */
    fun setShareBitmap(bitmap: Bitmap?) {
        content = ShareContent()
        val image = UMImage(context, bitmap)
        val thumb = UMImage(context, bitmap)
        image.setThumb(thumb) //设置缩略图 如果不设置 微信分享时的弹框不会显示分享的图片
        image.compressStyle = UMImage.CompressStyle.SCALE //大小压缩，默认为大小压缩，适合普通很大的图
        image.compressFormat = Bitmap.CompressFormat.JPEG
        content!!.mMedia = image
    }

    /**
     * @desc 设置分享链接
     * @author zhengjun
     * @created at 2018/8/30 14:35
     */
    fun setShareContent(shareTitle: String?, shareContent: String?, Url: String?) {
        content = ShareContent()
        val web = UMWeb(Url)
        web.title = shareTitle //标题
        val thumb = UMImage(context, "https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/share_logo.png")
        web.setThumb(thumb) //缩略图
        web.description = shareContent //描述
        content!!.mMedia = web
    }

    /**
     * @desc 分享到qq
     * @author zhengjun
     * @created at 2018/8/24 9:53
     */
    fun shareToQQ() {
        ShareAction(context as Activity).setPlatform(SHARE_MEDIA.QQ).setCallback(umShareListener)
            .setShareContent(content).share()
    }

    /**
     * @desc 分享到qq空间
     * @author zhengjun
     * @created at 2018/8/24 9:53
     */
    fun shareToQZone() {
        ShareAction(context as Activity).setPlatform(SHARE_MEDIA.QZONE).setCallback(umShareListener)
            .setShareContent(content).share()
    }

    /**
     * @desc 分享到微信
     * @author zhengjun
     * @created at 2018/8/24 9:53
     */
    fun shareToWeiXin() {
        ShareAction(context as Activity).setPlatform(SHARE_MEDIA.WEIXIN)
            .setCallback(umShareListener).setShareContent(content).share()
    }

    /**
     * @desc 分享到微信朋友圈
     * @author zhengjun
     * @created at 2018/8/24 9:53
     */
    fun shareToWeiXinCircle() {
        ShareAction(context as Activity).setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
            .setCallback(umShareListener).setShareContent(content).share()
    }

    /**
     * @desc 分享到新浪微博
     * @author zhengjun
     * @created at 2018/8/24 9:53
     */
    fun shareToSinaWb() {
        ShareAction(context as Activity).setPlatform(SHARE_MEDIA.SINA).setCallback(umShareListener)
            .setShareContent(content).share()
    }

    private val umShareListener: UMShareListener = object : UMShareListener {
        override fun onStart(share_media: SHARE_MEDIA) {
            Log.d(Tag, "分享开始")
        }

        override fun onResult(platform: SHARE_MEDIA) {
            Log.d(Tag, "分享成功")
        }

        override fun onError(platform: SHARE_MEDIA, t: Throwable) {
            Log.d("throw", "throw:" + t.message)
            showToast("分享失败:" + t.message)
        }

        override fun onCancel(platform: SHARE_MEDIA) {
            Log.d(Tag, "分享被用户主动取消")
        }
    }

    companion object {
        private const val Tag = "shareTag"
    }
}
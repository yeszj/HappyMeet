package cn.yanhu.dynamic.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.manager.VideoCacheProxy
import cn.yanhu.commonres.utils.VideoUtils
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.databinding.PopDynamicImageBinding
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import xyz.doikki.videoplayer.player.BaseVideoView
import xyz.doikki.videoplayer.player.VideoView

/**
 * @author: zhengjun
 * created: 2024/3/7
 * desc:
 */
@SuppressLint("ViewConstructor")
class CustomDynamicImagePopupView(
    context: Context, private val item: DynamicInfo,
    private val picPosition: Int
) : ImageViewerPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_dynamic_image
    }

    private lateinit var mBinding: PopDynamicImageBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDynamicImageBinding.bind(customView)
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                dismiss()
            }
            override fun rightButtonOnClick(v: View?) {
            }
        })
        mBinding.apply {
            momentInfo = item
            if (item.isAuth) {
                TextViewDrawableUtils.setDrawableRight(context, tvName, cn.yanhu.commonres.R.drawable.svg_identify_tag)
            } else {
                TextViewDrawableUtils.setDrawableRight(tvName, null)
            }
            setIndicatorStyle(picPosition, item)
            playVideo(item.images[picPosition])
        }
    }

     fun setIndicatorStyle(
        currentPosition: Int,
        item: DynamicInfo
    ) {
         mBinding.apply {
             val isInit: Boolean = vgIndicator.childCount <= 0
             if (item.images.size > 1) {
                 for (i in item.images.indices) {
                     val indicatorView = if (isInit) {
                         getIndicatorView(item.images.size)
                     } else {
                         vgIndicator.getChildAt(i)
                     }
                     indicatorView?.apply {
                         if (currentPosition == i) {
                             indicatorView.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_iamge_inditor_select)
                         } else {
                             indicatorView.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_image_inditor_normal)
                         }
                     }
                     if (isInit) {
                         vgIndicator.addView(indicatorView)
                     }
                 }
             }
         }
    }

    private fun getIndicatorView(size: Int): View {
        val dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
        val view = View(context)
        val width = (ScreenUtils.getAppScreenWidth() - dimension * size- CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)) / size
        val marginLayoutParams = ViewGroup.MarginLayoutParams(
            width,
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
        )
        marginLayoutParams.marginEnd = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
        view.layoutParams = marginLayoutParams
        return view
    }

    fun playVideo(url: String) {
        mBinding.videoView.apply {
            if (VideoUtils.isVideo(url)) {
                releaseVideoView()
                setVideoController(null)
                val proxyUrl = VideoCacheProxy.getCacheUrl(context, url)
                setUrl(proxyUrl)
                start()
                setOnStateChangeListener(object : BaseVideoView.OnStateChangeListener {
                    override fun onPlayerStateChanged(playerState: Int) {
                    }

                    override fun onPlayStateChanged(playState: Int) {
                        if (playState == VideoView.STATE_PLAYING) {
                            mBinding.videoView.visibility = View.VISIBLE
                        }
                    }
                })
            } else {
                if (isPlaying) {
                    mBinding.videoView.visibility = View.INVISIBLE
                    pause()
                }
            }
        }

    }

    private fun releaseVideoView() {
        mBinding.apply {
            videoView.visibility = View.INVISIBLE
            videoView.pause()
            videoView.release()
        }
    }


    override fun onDismiss() {
        super.onDismiss()
        releaseVideoView()
    }

    companion object {
        fun asImageViewer(
            context: Context,
            srcView: ImageView?,
            picPosition: Int,
            srcViewUpdateListener: OnSrcViewUpdateListener? = null,
            momentInfo: DynamicInfo,
        ): ImageViewerPopupView? {
            val longPressListener1 =
                CustomDynamicImagePopupView(context, momentInfo, picPosition)
                    .setSrcView(srcView, picPosition)
                    .setImageUrls(momentInfo.images as List<Any>?)
                    .isInfinite(false)
                    .isShowIndicator(false)
                    .isShowPlaceholder(false)
                    .isShowSaveButton(false)
                    .setSrcViewUpdateListener(srcViewUpdateListener)
                    .setXPopupImageLoader(SmartGlideImageLoader())
            XPopup.Builder(ActivityUtils.getTopActivity()).isDestroyOnDismiss(true).asCustom(longPressListener1).show()
            return longPressListener1
        }
    }
}
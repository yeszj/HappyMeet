@file:Suppress("NAME_SHADOWING")

package cn.yanhu.dynamic.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.ImageThumbUtils
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.GifLoadUtils
import cn.yanhu.commonres.utils.VideoUtils
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.databinding.AdapterDynamicVideoShowSingleItemBinding
import cn.yanhu.dynamic.pop.CustomDynamicImagePopupView
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.request
import com.blankj.utilcode.util.VibrateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * @author: zhengjun
 * created: 2023/6/29
 * desc:
 */
object MomentManager {
    const val LIKE = 1
    const val UNLIKE = 2

    /**
     * 删除评论确认弹框
     */
    fun showDeleteDiscussDialog(
        discussId: String,
        isSelf: Boolean,
        trendsId: String,
        onBooleanResultListener: OnBooleanResultListener,
    ) {
        DialogUtils.showConfirmDialog(
            "确认删除这条评论吗?",
            {
                if (isSelf) {
                    //自己删除评论
                    deleteDiscuss(
                        discussId, trendsId, onBooleanResultListener
                    )
                } else {
                    //管理员删除评论
                    adminOperateTrends(2, discussId, trendsId, onBooleanResultListener)
                }
            },
            {
            },
            cancel = "取消",
            confirm = "删除"
        )
    }

    /**
     * 删除动态确认弹框
     */
    fun showDeleteMomentDialog(
        mContext: Context,
        trendsId: String,
        isSelf: Boolean,
        onBooleanResultListener: OnBooleanResultListener? = null,
    ) {
        DialogUtils.showConfirmDialog(
            "确认删除这条动态吗？",
            {
                if (isSelf) {
                    //自己删除动态
                    deleteMoment(
                        trendsId,
                        onBooleanResultListener
                    )
                } else {
                    //管理员删除动态
                    adminOperateTrends(1, trendsId, trendsId, onBooleanResultListener)
                }
            },
            {
            },
            cancel = "取消",
            confirm = "删除",
        )
    }

    /**
     * 管理员删除动态/评论
     * operateType 1动态 2评论
     */
    private fun adminOperateTrends(
        operateType: Int,
        discussId: String,
        trendsId: String,
        onBooleanResultListener: OnBooleanResultListener?,
    ) {
        request(
            { momentRxApi.adminOperateTrends(operateType, discussId) },
            object : OnBooleanResultListener {
                override fun onSuccess() {
                    if (operateType == 1) {
                        deleteMomentSuccess(onBooleanResultListener, trendsId)
                    } else {
                        deleteDiscussSuccess(onBooleanResultListener, trendsId)
                    }
                }
            }
        )
    }


    /**
     * 自己删除评论
     */
    private fun deleteDiscuss(
        discussId: String,
        trendsId: String,
        onBooleanResultListener: OnBooleanResultListener,
    ) {
        request(
            { momentRxApi.userRemoveComment(discussId) },
            object : OnBooleanResultListener {
                override fun onSuccess() {
                    deleteDiscussSuccess(onBooleanResultListener, trendsId)
                }
            }
        )
    }

    /**
     * 删除评论成功
     */
    private fun deleteDiscussSuccess(
        onBooleanResultListener: OnBooleanResultListener?,
        trendsId: String,
    ) {
        onBooleanResultListener?.onSuccess()
        LiveDataEventManager.sendLiveDataMessage(
            LiveDataEventManager.DELETE_DISCUSS_SUCCESS,
            trendsId
        )
    }

    /**
     * 自己删除动态
     */
    private fun deleteMoment(trendsId: String, onBooleanResultListener: OnBooleanResultListener?) {
        request(
            { momentRxApi.removeTrends(trendsId) },
            object : OnBooleanResultListener {
                override fun onSuccess() {
                    deleteMomentSuccess(onBooleanResultListener, trendsId)
                }
            }
        )
    }

    /**
     * 删除动态成功
     */
    private fun deleteMomentSuccess(
        onBooleanResultListener: OnBooleanResultListener?,
        trendsId: String,
    ) {
        showToast("已删除")
        onBooleanResultListener?.onSuccess()
        LiveDataEventManager.sendLiveDataMessage(
            LiveDataEventManager.DELETE_MOMENT_SUCCESS,
            trendsId
        )
    }

    fun praiseMoment(trendsId: String, type: Int) {
        if (type == LIKE) {
            VibrateUtils.vibrate(30)
        }
        if (type == LIKE) {
            LiveDataEventManager.sendLiveDataMessage(
                LiveDataEventManager.LIKE_MOMENT_SUCCESS,
                trendsId
            )
        } else {
            LiveDataEventManager.sendLiveDataMessage(
                LiveDataEventManager.UNLIKE_MOMENT_SUCCESS,
                trendsId
            )
        }
        request(
            { momentRxApi.praiseMoment(trendsId, type) },
            object : OnBooleanResultListener {
                override fun onSuccess() {
                }
            },
            isShowToast = false
        )
    }

    fun praiseDiscuss(trendsId: String, commentId: String, type: Int) {
        request(
            { momentRxApi.praiseDiscuss(trendsId, commentId, type) },
            object : OnBooleanResultListener {
                override fun onSuccess() {
                }
            },
            isShowToast = false
        )
    }

    @SuppressLint("CheckResult")
    fun showSingleImage(
        context: Context,
        vgSingleVideo: AdapterDynamicVideoShowSingleItemBinding,
        momentInfo: DynamicInfo,
    ) {
        vgSingleVideo.vgVideoParent.visibility = View.VISIBLE
        val images = momentInfo.images
        val url = images[0]
        if (url.endsWith(".gif")) {
            vgSingleVideo.ivBg.maxWidth = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_240)
            vgSingleVideo.ivBg.maxHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_150)
            GifLoadUtils.loadGif(ImageThumbUtils.getThumbUrl(url), vgSingleVideo.ivBg)
        } else {
            Glide.with(context).asBitmap().load(ImageThumbUtils.getThumbUrl(url))
                .centerCrop()
                .placeholder(vgSingleVideo.ivBg.drawable)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap?>?,
                    ) {
                        setSingleImageWH(bitmap, vgSingleVideo.ivBg)
                        vgSingleVideo.ivBg.scaleType = ImageView.ScaleType.CENTER_CROP
                        val roundDrawable =
                            RoundedBitmapDrawableFactory.create(context.resources, bitmap)
                        roundDrawable.cornerRadius =
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4).toFloat()
                        vgSingleVideo.ivBg.setImageDrawable(roundDrawable)
                        if (VideoUtils.isVideo(url)) {
                            vgSingleVideo.ivVideo.visibility = View.VISIBLE
                        }else{
                            vgSingleVideo.ivVideo.visibility = View.INVISIBLE
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
        vgSingleVideo.vgVideo.setOnClickListener {
            CustomDynamicImagePopupView.asImageViewer(context,vgSingleVideo.ivBg,0,momentInfo = momentInfo )
        }
    }

    fun setSingleImageWH(bitmap: Bitmap, iv: ImageView) {
        val parentWidth = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_278)
        val w = bitmap.width
        val h = bitmap.height
        val newW: Int
        val newH: Int
        if (h > w * 3) { //h:w = 5:3
            newW = parentWidth / 2
            newH = newW * 5 / 3
        } else if (h < w) { //h:w = 2:3
            newW = parentWidth * 2 / 3
            newH = newW * 2 / 3
        } else { //newH:h = newW :w
            newW = parentWidth / 2
            newH = h * newW / w
        }
        val layoutParams = iv.layoutParams
        layoutParams.width = newW
        layoutParams.height = newH
        iv.layoutParams = layoutParams
    }
}
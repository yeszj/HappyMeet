package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.lxj.xpopup.impl.FullScreenPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopRoomAngleResultBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.utils.SVGAUtils
import cn.yanhu.commonres.utils.VideoAnimUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.inter.IFetchResource
import com.tencent.qgame.animplayer.mix.Resource
import kotlin.collections.forEach

/**
 * @author: zhengjun
 * created: 2024/11/21
 * desc:
 */
@SuppressLint("ViewConstructor")
class RoomAngleResultPop(
    context: Context,
    private val angleUser: BaseUserInfo?,
    private val guardUser: BaseUserInfo?,
    val type: Int
) : FullScreenPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_room_angle_result
    }

    private lateinit var mBinding: PopRoomAngleResultBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRoomAngleResultBinding.bind(popupImplView)
        if (angleUser!!.level == 1) {
            mBinding.svgaImage.visibility = VISIBLE
            mBinding.animView.visibility = INVISIBLE
            SVGAUtils.loadCustomAssetsSVGAAnim(
                if (type == CrownedUserListPop.TYPE_ANGLE) "angle_anim.svga" else "singer_anim.svga",
                object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        val dynamicItem = SVGADynamicEntity()
                        val drawable = SVGADrawable(videoItem, dynamicItem)
                        angleUser.apply {
                            val drawableIdByName = if (type == CrownedUserListPop.TYPE_ANGLE) {
                                ResourceUtils.getDrawableIdByName("angel_" + angleUser.level)
                            } else {
                                ResourceUtils.getDrawableIdByName("singer_" + angleUser.level)
                            }
                            val titleDrawable = ResourceUtils.getDrawable(drawableIdByName)
                            dynamicItem.setDynamicImage(titleDrawable.toBitmap(), "title")

                            dynamicItem.setDynamicImage(angleUser.portrait, "tianshi")
                        }
                        guardUser?.apply {
                            dynamicItem.setDynamicImage(guardUser.portrait, "shouhu")
                        }

                        mBinding.svgaImage.setImageDrawable(drawable)
                        mBinding.svgaImage.startAnimation()
                    }

                    override fun onError() {
                    }
                })
            mBinding.svgaImage.callback = object : SVGACallback {
                override fun onFinished() {
                    dismiss()
                }

                override fun onPause() {
                }

                override fun onRepeat() {
                }

                override fun onStep(frame: Int, percentage: Double) {
                }
            }
        } else {
            mBinding.svgaImage.visibility = INVISIBLE
            mBinding.animView.visibility = VISIBLE
            mBinding.animView.setFetchResource(object : IFetchResource {
                override fun fetchImage(
                    resource: Resource,
                    result: (Bitmap?) -> Unit
                ) {
                    val srcTag = resource.tag
                    if (srcTag.isNotEmpty()) {
                        if ("title" == srcTag) {
                            val drawableIdByName = if (type == CrownedUserListPop.TYPE_ANGLE) {
                                ResourceUtils.getDrawableIdByName("angel_" + angleUser.level)
                            } else {
                                ResourceUtils.getDrawableIdByName("singer_" + angleUser.level)
                            }
                            val titleDrawable =
                                ResourceUtils.getDrawable(drawableIdByName).toBitmap()
                            result(titleDrawable)
                        } else if ("tianshi" == srcTag) {
                            val portrait = angleUser.portrait
                            GlideUtils.loadAsBitmap(
                                context,
                                portrait,
                                object :
                                    CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        result(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        result(null)
                                    }
                                })
                        } else if ("shouhu" == srcTag) {
                            if (guardUser != null) {
                                val portrait = guardUser.portrait
                                GlideUtils.loadAsBitmap(
                                    context,
                                    portrait,
                                    object :
                                        CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            result(resource)
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                        }

                                        override fun onLoadFailed(errorDrawable: Drawable?) {
                                            result(null)
                                        }
                                    })
                            } else {
                                result(null)
                            }
                        }
                    } else {
                        result(null)
                    }
                }

                override fun fetchText(
                    resource: Resource,
                    result: (String?) -> Unit
                ) {
                    result(null)
                }

                override fun releaseResource(resources: List<Resource>) {
                    resources.forEach {
                        it.bitmap?.recycle()
                        it.bitmap = null
                    }
                }

            })
            mBinding.animView.setAnimListener(object : IAnimListener {
                override fun onFailed(errorType: Int, errorMsg: String?) {
                    ThreadUtils.getMainHandler().post {
                        dismiss()
                    }
                }

                override fun onVideoComplete() {
                    ThreadUtils.getMainHandler().post {
                        dismiss()
                    }
                }

                override fun onVideoDestroy() {
                }

                override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
                }

                override fun onVideoStart() {
                }
            })
            val animAssetsName =  "singer_anim_" + angleUser.level+".mp4"
            VideoAnimUtils.loadAssetsVideoAnim(
                context,
                animAssetsName,
                mBinding.animView,
                object : VideoAnimUtils.OnLoadVideoAnimListener {
                    override fun onLoadFail() {
                        dismiss()
                    }
                })
        }

    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        mBinding.svgaImage.clear()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            angleUser: BaseUserInfo?,
            guardUser: BaseUserInfo?,
            type: Int,
            simpleCallback: SimpleCallback
        ): RoomAngleResultPop {
            val matchPop = RoomAngleResultPop(mContext, angleUser, guardUser, type)
            val builder =
                XPopup.Builder(mContext)
                    .setPopupCallback(simpleCallback)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
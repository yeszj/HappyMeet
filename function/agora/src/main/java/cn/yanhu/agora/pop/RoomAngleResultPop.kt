package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.graphics.drawable.toBitmap
import com.lxj.xpopup.impl.FullScreenPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopRoomAngleResultBinding
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.utils.SVGAUtils
import com.blankj.utilcode.util.ResourceUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

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
        SVGAUtils.loadCustomAssetsSVGAAnim("angle_anim.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val dynamicItem = SVGADynamicEntity()
                val drawable = SVGADrawable(videoItem, dynamicItem)
                angleUser?.apply {
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
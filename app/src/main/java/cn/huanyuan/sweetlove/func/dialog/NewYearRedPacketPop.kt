package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import cn.huanyuan.sweetlove.databinding.PopNewYearRedPacketBinding
import cn.yanhu.baselib.anim.Rotate3dAnimation
import cn.yanhu.baselib.anim.SpringAnimationManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.RedPacketRewardInfo
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.SimpleCallback


/**
 * @author: zhengjun
 * created: 2025/1/17
 * desc:
 */
@SuppressLint("ViewConstructor")
class NewYearRedPacketPop(context: Context, val drawable: Drawable) : CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            drawable: Drawable,
            simpleCallback: SimpleCallback
        ): NewYearRedPacketPop {
            val remarkNameTipsDialog = NewYearRedPacketPop(context, drawable)
            val builder = XPopup.Builder(context)
            builder.setPopupCallback(simpleCallback)
            builder
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .maxWidth(ScreenUtils.getScreenWidth())
                .maxHeight(ScreenUtils.getScreenHeight())
                .asCustom(remarkNameTipsDialog).show()
            return remarkNameTipsDialog
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_new_year_red_packet
    }

    private lateinit var mBinding: PopNewYearRedPacketBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopNewYearRedPacketBinding.bind(popupImplView)
        ViewUtils.setViewWidth(mBinding.ivImage,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_360))
        mBinding.ivImage.setImageDrawable(drawable)
        playTreeAnimation()
        mBinding.ivImage.setOnSingleClickListener {
            if (rewardDrawable!=null){
                dismiss()
            }else{
                receiveRedPacket()
            }
        }
    }

    private var rewardInfo: RedPacketRewardInfo? = null
    private var rewardDrawable: Drawable? = null
    private fun receiveRedPacket() {
        request({ rxApi.receiveRedPacket() },
            object : OnRequestResultListener<RedPacketRewardInfo> {
                override fun onSuccess(data: BaseBean<RedPacketRewardInfo>) {
                    rewardInfo = data.data
                    if (rewardInfo == null) {
                        return
                    }
                    GlideUtils.loadAsDrawable(
                        context,
                        rewardInfo!!.bgUrl,
                        object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                rewardDrawable = resource
                                mBinding.vgParent.clearAnimation()
                                playRotationAnim(0f, 180f)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    dismiss()
                }
            })
    }

    private fun playTreeAnimation() {
        val animation = SpringAnimationManager.getAnimation(1.1f,1.1f,0.5f)
        mBinding.vgParent.startAnimation(animation)
    }

    private fun playRotationAnim(start: Float, end: Float) {
        val centerX: Float = this.width / 2.0f
        val centerY: Float = this.height / 2.0f
        //括号内参数分别为（上下文，开始角度，结束角度，x轴中心点，y轴中心点，深度，是否扭曲）
        val rotation =
            Rotate3dAnimation(context, start, end, centerX, centerY, 1.0f, true)
        rotation.duration = 500 //设置动画时长
        rotation.fillAfter = true //保持旋转后效果
        AccelerateInterpolator().also { rotation.interpolator = it } //设置插值器
        rotation.setAnimationListener(object : Animation.AnimationListener {
            //设置监听器
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                playRotationAnim(180f, 0f)
                mBinding.tvDesc.visibility = View.VISIBLE
                mBinding.tvDesc.text = rewardInfo?.rewardDesc
                ViewUtils.setViewWidth(mBinding.ivImage,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_280))
                mBinding.ivImage.setImageDrawable(rewardDrawable)
                playTreeAnimation()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        mBinding.vgParent.startAnimation(rotation)
    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        mBinding.vgParent.clearAnimation()
    }
}
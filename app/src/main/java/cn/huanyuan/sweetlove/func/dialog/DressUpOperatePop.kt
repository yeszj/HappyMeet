package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopDressUpOperateBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.userinfo.dressUp.DressUpFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.DressUpInfo
import cn.yanhu.commonres.bean.request.DressUpRequest
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.utils.SVGAUtils
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.request
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.opensource.svgaplayer.SVGACallback

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
@SuppressLint("ViewConstructor")
class DressUpOperatePop(
    val context: FragmentActivity,
    val item: DressUpInfo,
    val type: Int,
    private val roseBalance: String,
    private val onDressUpListener: OnDressUpListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_dress_up_operate
    }

    private var mBinding: PopDressUpOperateBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDressUpOperateBinding.bind(popupImplView)
        mBinding?.apply {
            itemInfo = item
            val svga = item.svgUrl


            if (!TextUtils.isEmpty(svga)) {
                if (type != DressUpFrg.TYPE_CAR) {
                    if (type == DressUpFrg.TYPE_USER_FLOAT) {
                        ViewUtils.setViewSize(
                            ivGoods,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_187),
                            CommonUtils.getDimension(
                                com.zj.dimens.R.dimen.dp_50
                            )
                        )
                    }
                    tvRead.visibility = View.INVISIBLE
                    SVGAUtils.loadSVGAAnim(ivGoods, svga)
                } else {
                    tvRead.visibility = View.VISIBLE
                    vgPic.setOnSingleClickListener {
                        SVGAUtils.loadSVGAAnim(svgaImageView, svga)
                    }
                    // 设置回调
                    svgaImageView.callback = object : SVGACallback {
                        override fun onPause() {
                        }

                        override fun onFinished() {
                            svgaImageView.clear()
                        }

                        override fun onRepeat() {}
                        override fun onStep(i: Int, v: Double) {}
                    }
                }
            } else {
                tvRead.visibility = View.INVISIBLE
            }
            loadDrawable()
            tvBuy.setOnSingleClickListener {
                if (item.isTimeOut) {
                    DressGoodsBuyPop.showDialog(context, item, type, roseBalance)
                    dismiss()
                } else {
                    startWear(item.isWear)
                }
            }
            vgOutline.setOnSingleClickListener { dismiss() }
            vgContent.setOnSingleClickListener { }
        }
    }

    private fun loadDrawable() {
        GlideUtils.loadAsDrawable(context, item.cover, object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable, transition: Transition<in Drawable>?
            ) {
                mBinding!!.ivGoods.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })
    }

    private fun startWear(isWear: Boolean) {
        val buyRequest = DressUpRequest(
            item.id,
            if (isWear) 0 else 1,
        )
        request({ rxApi.dressUpCommodity(buyRequest) }, object : OnBooleanResultListener {
            override fun onSuccess() {
                if (isWear) {
                    showToast("取消佩戴成功")
                } else {
                    showToast("佩戴成功")
                }
                LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_USER_INFO).post(true)
                item.isWear = !isWear
                mBinding?.itemInfo = item
                onDressUpListener.onDressUpSuccess(item.id)
            }
        })
    }


    interface OnDressUpListener{
        fun onDressUpSuccess(id:Int)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity, item: DressUpInfo, type: Int, roseBalance: String,onDressUpListener: OnDressUpListener
        ): DressUpOperatePop {
            val matchPop = DressUpOperatePop(mContext, item, type, roseBalance,onDressUpListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}
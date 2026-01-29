package cn.yanhu.commonres.pop

import android.R.attr.resource
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.CommonTipsInfo
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.DialogCommonTipsBinding
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.PageIntentUtil
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2023/11/21
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonTipDialog(
    context: Context,
    private val commonTipsInfo: CommonTipsInfo,
    private val onClickBtnListener: OnClickBtnListener?
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_common_tips
    }

    private var mBinding: DialogCommonTipsBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = DialogCommonTipsBinding.bind(popupImplView)
        mBinding?.apply {
            tipsInfo = commonTipsInfo
            if (TextUtils.isEmpty(commonTipsInfo.desc)){
                tvDesc.visibility = GONE
            }else{
                tvDesc.visibility = VISIBLE
            }
            if (commonTipsInfo.isShowService){
                tvService.visibility = VISIBLE
                tvService.setOnSingleClickListener {
                    PageIntentUtil.url2Page(context, PageIntentUtil.TYPE_CONTACT_UNION)
                }
            }else{
                tvService.visibility = GONE
            }

            if (commonTipsInfo.drawableId==0){
                if (TextUtils.isEmpty(commonTipsInfo.icon)) {
                    ivTipIcon.visibility = GONE
                    ViewUtils.setMarginTop(viewBg, 0)
                    ViewUtils.setPaddingTop(
                        tvTitle,
                        CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)
                    )
                } else {
                    GlideUtils.loadAsDrawable(
                        context,
                        commonTipsInfo.icon){
                        ivTipIcon.setImageDrawable(it)
                    }
                    ivTipIcon.visibility = VISIBLE
                    ViewUtils.setPaddingTop(
                        tvTitle,
                        CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10)
                    )
                    ViewUtils.setMarginTop(
                        viewBg,
                        CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_70)
                    )
                }
            }else{
                ivTipIcon.setImageResource(commonTipsInfo.drawableId)
                ivTipIcon.visibility = View.VISIBLE
                ViewUtils.setPaddingTop(tvTitle,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10))
                ViewUtils.setMarginTop(viewBg,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_70))
            }
            ivClose.setOnClickListener {
                dismiss()
                onClickBtnListener?.onClickCancel()
            }
            btnNext.setOnClickListener {
                if (commonTipsInfo.isAutoDismiss) {
                    dismiss()
                }
                if (TextUtils.isEmpty(commonTipsInfo.url)) {
                    onClickBtnListener?.onClickBtn()
                } else {
                    PageIntentUtil.url2Page(context, commonTipsInfo.url)
                }            }
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.FACE_RESULT).observe(this) {
            if (it){
                dismiss()
            }
        }
    }

    fun setDesc(value:CharSequence){
        mBinding?.tvDesc?.text = value
    }

    interface OnClickBtnListener {
        fun onClickBtn()
        fun onClickCancel() {}
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            commonTipsInfo: CommonTipsInfo,
            onClickBtnListener: OnClickBtnListener? = null,
            simpleCallback: SimpleCallback? = null
        ): CommonTipDialog {
            val remarkNameTipsDialog = CommonTipDialog(context, commonTipsInfo, onClickBtnListener)
            val builder = XPopup.Builder(context)
            if (simpleCallback != null) {
                builder.setPopupCallback(simpleCallback)
            }
            builder
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)

                .asCustom(remarkNameTipsDialog).show()
            return remarkNameTipsDialog
        }
    }
}
package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.bean.CommonTipsInfo
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.DialogCommonTipsBinding

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
            if (commonTipsInfo.drawableId==0){
                ivTipIcon.visibility = View.GONE
                ViewUtils.setMarginTop(viewBg,0)
                ViewUtils.setPaddingTop(tvTitle, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
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
                onClickBtnListener?.onClickBtn()
            }
        }
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
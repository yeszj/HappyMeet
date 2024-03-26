package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.R
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.animator.TranslateAnimator
import com.lxj.xpopup.core.PositionPopupView
import com.lxj.xpopup.enums.PopupAnimation

/**
 * @author: zhengjun
 * desc:权限弹框
 */
@SuppressLint("ViewConstructor")
class CommonPermissionPop(
    context: Context,
    private val tips: String
) : PositionPopupView(context) {

    private var tvTips: TextView? = null

    override fun getImplLayoutId(): Int {
        return R.layout.pop_common_permission
    }
    override fun onCreate() {
        tvTips = findViewById(R.id.tv_tips)
        tvTips?.text = tips
    }

    override fun getPopupAnimator(): PopupAnimator {
        return TranslateAnimator(
            popupContentView,
            animationDuration,
            PopupAnimation.TranslateFromTop
        )
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            tips:String
        ): CommonPermissionPop {
            val matchPop = CommonPermissionPop(mContext,tips)
            val builder = XPopup.Builder(mContext)
            builder.dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .enableDrag(false)
                .hasShadowBg(false)
                .isViewMode(true)
                .enableShowWhenAppBackground(true)
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }
}
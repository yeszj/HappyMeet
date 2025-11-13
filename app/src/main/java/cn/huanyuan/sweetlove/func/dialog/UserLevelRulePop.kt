package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import com.lxj.xpopup.core.CenterPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopUserLevelRuleBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/2/19
 * desc:
 */
@SuppressLint("ViewConstructor")
class UserLevelRulePop(
    context: Context, private val ruleDrawable: Drawable, private val wealthDrawable: Drawable
) : CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Activity, ruleDrawable: Drawable, wealthDrawable: Drawable
        ): UserLevelRulePop {
            val sexPop = UserLevelRulePop(mContext, ruleDrawable, wealthDrawable)
            val builder = XPopup.Builder(mContext).maxHeight(ScreenUtils.getScreenHeight())
            builder.isDestroyOnDismiss(true)
                .asCustom(sexPop)
                .show()
            return sexPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_user_level_rule
    }

    private lateinit var mBinding: PopUserLevelRuleBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopUserLevelRuleBinding.bind(popupImplView)
        changeSelect(0)
        mBinding.tvDismiss.setOnSingleClickListener {
            dismiss()
        }
        mBinding.vgRule.setOnSingleClickListener {
            changeSelect(0)
        }
        mBinding.vgWealth.setOnSingleClickListener {
            changeSelect(1)
        }
    }

    private fun changeSelect(position: Int) {
        mBinding.selectPosition = position
        if (position == 0) {
            mBinding.ivRule.setImageDrawable(ruleDrawable)
        } else {
            mBinding.ivRule.setImageDrawable(wealthDrawable)
        }
    }
}
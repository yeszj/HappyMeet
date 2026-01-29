package cn.huanyuan.sweetlove.func.dialog

import android.R.attr.resource
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.LevelPrivilegeInfo
import cn.huanyuan.sweetlove.databinding.PopUserPrivilegeExampleBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

/**
 * @author: zhengjun
 * created: 2024/4/3
 * desc:
 */
@SuppressLint("ViewConstructor")
class UserPrivilegeExamplePop(context: Context, private var levelPrivilegeInfo: LevelPrivilegeInfo) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_user_privilege_example
    }

    private var mBinding:PopUserPrivilegeExampleBinding?=null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopUserPrivilegeExampleBinding.bind(popupImplView)
        mBinding?.apply {
            privilegeInfo = levelPrivilegeInfo
            GlideUtils.loadAsDrawable(context,levelPrivilegeInfo.privilegeShowExampleIcon) {
                ivExample.setImageDrawable(it)
            }
            if (TextUtils.isEmpty(levelPrivilegeInfo.privilegeLevelAllStyleIcon)){
                ivStyle.visibility = View.GONE
            }else{
                ivStyle.visibility = View.VISIBLE
                GlideUtils.loadAsDrawable(context,levelPrivilegeInfo.privilegeLevelAllStyleIcon){
                    ivStyle.setImageDrawable(it)
                }
            }
            tvSure.setOnSingleClickListener { dismiss() }
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            levelPrivilegeInfo: LevelPrivilegeInfo
        ): UserPrivilegeExamplePop {
            val matchPop = UserPrivilegeExamplePop(mContext, levelPrivilegeInfo)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }
}
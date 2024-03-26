package cn.huanyuan.happymeet.func.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.lxj.xpopup.core.PositionPopupView
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.PopUserFilterBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.pop.CommonWheelView2Pop
import cn.yanhu.commonres.pop.CommonWheelViewPop
import com.blankj.utilcode.util.StringUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.animator.TranslateAnimator
import com.lxj.xpopup.enums.PopupAnimation

/**
 * @author: zhengjun
 * created: 2024/3/1
 * desc:
 */
@SuppressLint("ViewConstructor")
class UserFilterPop(
    context: Context,
    var selectCity: String,
    var selectAge: String,
    private val onFilterValueListener: OnFilterValueListener
) :
    PositionPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_user_filter
    }

    private lateinit var mBinding: PopUserFilterBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopUserFilterBinding.bind(popupImplView)
        mBinding.apply {
            tvAge.text = if (TextUtils.isEmpty(selectAge)) "不限" else selectAge
            tvProvince.text = if (TextUtils.isEmpty(selectCity)) "不限" else selectCity
            tvProvince.setOnSingleClickListener {
                showSelectProvincePop()
            }
            tvAge.setOnSingleClickListener {
                showSelectAgePop()
            }
            btnReSet.setOnSingleClickListener {
                resetFilterValue()
            }
            btnSure.setOnSingleClickListener {
                onFilterValueListener.onFilterValue(selectCity, selectAge)
                dismiss()
            }
        }
    }

    private fun showSelectAgePop(): CommonWheelView2Pop {
        val list = mutableListOf<String>()
        for (i in 18..70) {
            list.add(i.toString())
        }
        var selectValue = ""
        var selectValue2 = ""
        if (!TextUtils.isEmpty(selectAge)){
            val split = selectAge.split("-")
            selectValue = split[0].trim()
            selectValue2 = split[1].trim()
        }
        return CommonWheelView2Pop.showDialog(
            context,
            list,
            list,
            selectValue,
            selectValue2,
            object : CommonWheelView2Pop.OnSelectWheelListener {
                override fun onSelectValue(value: String, value2: String) {
                    selectAge = if (CommonUtils.compareString(value,value2)){
                        "$value2 - $value"
                    }else{
                        "$value - $value2"
                    }
                    mBinding.tvAge.text = selectAge
                }
            })
    }

    private fun PopUserFilterBinding.showSelectProvincePop(): CommonWheelViewPop {
        val stringArray =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.address_list)
        return CommonWheelViewPop.showDialog(
            context,
            stringArray.toMutableList(),
            selectCity,
            object : CommonWheelViewPop.OnSelectWheelListener {
                override fun onSelectValue(value: String) {
                    selectCity = value
                    tvProvince.text = value
                }
            })
    }

    private fun PopUserFilterBinding.resetFilterValue() {
        tvProvince.text = "不限"
        tvAge.text = "不限"
        selectCity = ""
        selectAge = ""
    }

    override fun getPopupAnimator(): PopupAnimator {
        return TranslateAnimator(
            popupContentView,
            animationDuration,
            PopupAnimation.TranslateFromTop
        )
    }

    interface OnFilterValueListener {
        fun onFilterValue(province: String, age: String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            selectCity: String,
            selectAge: String,
            onFilterValueListener: OnFilterValueListener
        ): UserFilterPop {
            val matchPop = UserFilterPop(mContext, selectCity, selectAge, onFilterValueListener)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }

}
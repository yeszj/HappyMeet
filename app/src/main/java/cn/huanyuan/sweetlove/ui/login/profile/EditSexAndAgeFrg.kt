package cn.huanyuan.sweetlove.ui.login.profile

import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgEditSexAndAgeBinding
import cn.huanyuan.sweetlove.ui.login.LoginViewModel
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.SexManager
import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener
import com.github.gzuliyujiang.wheelview.widget.WheelView

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
class EditSexAndAgeFrg : BaseFragment<FrgEditSexAndAgeBinding, LoginViewModel>(
    R.layout.frg_edit_sex_and_age,
    LoginViewModel::class.java
) {
    private lateinit var selfViewModel: LoginViewModel
    override fun initData() {
        selfViewModel = (context as CompleteProfileActivity).mViewModel
        mBinding.viewModel = selfViewModel
        mBinding.tvTips2.text = "性别选定后将无法修改哦\uD83E\uDD14"
        selectAge()
    }

    override fun initListener() {
        val listener = View.OnClickListener { v ->
            val personInfo: BaseUserInfo? =
                selfViewModel.personInfo.value
            if (v.id == R.id.selfManImage) {
                personInfo?.gender = SexManager.SEX_MAN
                ViewUtils.setViewPadding(mBinding.selfManImage, 0)
                mBinding.selfWomanImage.setPadding(
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18),
                    0,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18),
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_12)
                )
            } else if (v.id == R.id.selfWomanImage) {
                personInfo?.gender = SexManager.SEX_WOMAN
                mBinding.selfManImage.setPadding(
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18),
                    0,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_18),
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_12)
                )
                ViewUtils.setViewPadding(mBinding.selfWomanImage, 0)
            }
        }
        mBinding.selfManImage.setOnClickListener(listener)
        mBinding.selfWomanImage.setOnClickListener(listener)
    }

    private var isRemove: Boolean = false
    private fun selectAge() {
        val arrayList: MutableList<String> = ArrayList()
        for (i in 19..60) {
            arrayList.add(i.toString() + "岁")
        }
        arrayList.add(14, "滑动选择")
        var defaultPosition = 14
        mBinding.wheelView.apply {
            setData(arrayList)
            val indexOf = arrayList.indexOf("滑动选择")
            setDefaultPosition(indexOf)
            wheelView.setOnWheelChangedListener(object : OnWheelChangedListener {
                override fun onWheelScrolled(view: WheelView?, offset: Int) {
                }

                override fun onWheelSelected(view: WheelView?, position: Int) {
                    val item = arrayList[position]
                    if ("滑动选择" != item) {
                        defaultPosition = position
                        val selfViewModel = (context as CompleteProfileActivity).mViewModel
                        val replace = item.replace("岁", "")
                        selfViewModel.personInfo.value?.age = replace.toInt()
                    }
                }

                override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {
                    if (!isRemove) {
                        isRemove = true
                        arrayList.removeAt(indexOf)
                        setData(arrayList)
                        setDefaultPosition(defaultPosition)
                    }
                }

                override fun onWheelLoopFinished(view: WheelView?) {
                }
            })
        }
    }

    companion object {
        fun newsInstance(): EditSexAndAgeFrg {
            return EditSexAndAgeFrg()
        }
    }
}
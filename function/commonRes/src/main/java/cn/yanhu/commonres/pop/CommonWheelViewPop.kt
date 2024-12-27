package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener
import com.github.gzuliyujiang.wheelview.widget.WheelView
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.PopCommonWheelViewBinding
import com.github.gzuliyujiang.wheelpicker.widget.OptionWheelLayout
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/3/1
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonWheelViewPop(
    context: Context,
    val arrayList: MutableList<String>,
    private val selectValue: String,
    private val onSelectResultListener: OnSelectWheelListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_common_wheel_view
    }

    private lateinit var mBinding: PopCommonWheelViewBinding
    private var currentSelectValue: String = selectValue
    override fun onCreate() {
        super.onCreate()
        mBinding = PopCommonWheelViewBinding.bind(popupImplView)
        mBinding.wheelView.apply {
            setWheelData()
        }
        mBinding.tvCancel.setOnSingleClickListener { dismiss() }
        mBinding.tvSure.setOnSingleClickListener {
            onSelectResultListener.onSelectValue(currentSelectValue)
            dismiss()
        }
    }

    private fun OptionWheelLayout.setWheelData() {
        setData(arrayList)
        var indexOf = if (!TextUtils.isEmpty(selectValue)) {
            arrayList.indexOf(selectValue)
        } else {
            if (arrayList.size>2){
                2
            }else{
                0
            }
        }
        if (indexOf == -1) {
            indexOf = if (arrayList.size>2){
                2
            }else{
                0
            }
        }
        currentSelectValue = arrayList[indexOf]
        setDefaultPosition(indexOf)
        wheelView.setOnWheelChangedListener(object : OnWheelChangedListener {
            override fun onWheelScrolled(view: WheelView?, offset: Int) {
            }

            override fun onWheelSelected(view: WheelView?, position: Int) {
                currentSelectValue = arrayList[position]
            }

            override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {
            }

            override fun onWheelLoopFinished(view: WheelView?) {
            }
        })
    }

    interface OnSelectWheelListener {
        fun onSelectValue(value: String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            list: MutableList<String>,
            selectValue: String,
            onSelectResultListener: OnSelectWheelListener
        ): CommonWheelViewPop {
            val pop = CommonWheelViewPop(mContext, list, selectValue, onSelectResultListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(pop).show()
            return pop
        }
    }
}
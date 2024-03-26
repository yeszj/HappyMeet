package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener
import com.github.gzuliyujiang.wheelview.widget.WheelView
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.PopCommonWheelView2Binding
import com.github.gzuliyujiang.wheelpicker.widget.OptionWheelLayout
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/3/1
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonWheelView2Pop(
    context: Context,
    val arrayList: MutableList<String>,
    val arrayList2: MutableList<String>,
    private val selectValue: String,
    private val selectValue2: String,
    private val onSelectResultListener: OnSelectWheelListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_common_wheel_view2
    }

    private lateinit var mBinding: PopCommonWheelView2Binding
    private var currentSelectValue: String = selectValue
    private var currentSelectValue2: String = selectValue2
    override fun onCreate() {
        super.onCreate()
        mBinding = PopCommonWheelView2Binding.bind(popupImplView)
        mBinding.wheelView.apply {
            setWheelData(true)
        }
        mBinding.wheelView2.apply {
            setWheelData(false)
        }
        mBinding.tvCancel.setOnSingleClickListener { dismiss() }
        mBinding.tvSure.setOnSingleClickListener {
            onSelectResultListener.onSelectValue(currentSelectValue,currentSelectValue2)
            dismiss()
        }
    }

    private fun OptionWheelLayout.setWheelData(isFirst:Boolean) {
        var currentSelect = ""
        val list = if (isFirst){
            currentSelect = selectValue
            arrayList
        }else{
            currentSelect = selectValue2
            arrayList2
        }
        setData(list)
        var indexOf = if (!TextUtils.isEmpty(currentSelect)) {
            list.indexOf(currentSelect)
        } else {
            2
        }
        if (indexOf == -1) {
            indexOf = 2
        }
        if (isFirst){
            currentSelectValue = list[indexOf]
        }else{
            currentSelectValue2 = list[indexOf]
        }
        setDefaultPosition(indexOf)
        wheelView.setOnWheelChangedListener(object : OnWheelChangedListener {
            override fun onWheelScrolled(view: WheelView?, offset: Int) {
            }

            override fun onWheelSelected(view: WheelView?, position: Int) {
                if (isFirst){
                    currentSelectValue = list[position]
                }else{
                    currentSelectValue2 = list[position]
                }
            }

            override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {
            }

            override fun onWheelLoopFinished(view: WheelView?) {
            }
        })
    }

    interface OnSelectWheelListener {
        fun onSelectValue(value: String,value2:String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            list: MutableList<String>,
            list2: MutableList<String>,
            selectValue: String,
            selectValue2: String,
            onSelectResultListener: OnSelectWheelListener
        ): CommonWheelView2Pop {
            val pop = CommonWheelView2Pop(mContext, list,list2, selectValue,selectValue2, onSelectResultListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(pop).show()
            return pop
        }
    }
}
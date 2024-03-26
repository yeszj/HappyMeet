package cn.yanhu.commonres.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.commonres.R
import cn.yanhu.commonres.adapter.PayWayAdapter
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.databinding.ViewPayWaySelectBinding

/**
 * @author: zhengjun
 * created: 2024/3/7
 * desc:
 */
class PayWaySelectView : LinearLayout {

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private val payWayAdapter by lazy { PayWayAdapter() }
    private var mBinding: ViewPayWaySelectBinding? = null
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_pay_way_select, this, true
        )
        mBinding?.apply {
            rvPay.adapter = payWayAdapter
            val list:MutableList<PayWayInfo> = mutableListOf()
            list.add(PayWayInfo("支付宝",PayWayInfo.TYPE_ALIPAY,R.drawable.icon_alipay_logo))
            list.add(PayWayInfo("微信支付",PayWayInfo.TYPE_WXPAY,R.drawable.icon_wxpay_logo))
            payWayAdapter.submitList(list)
            payWayAdapter.setOnItemClickListener { _, _, position ->
                payWayAdapter.setSelectPosition(
                    position
                )
                onSelectChangeListener?.onSelectChange(payWayAdapter.getSelectPayType())
            }
        }
    }


    fun getSelectType():Int{
       return payWayAdapter.getSelectPayType()
    }


    private var onSelectChangeListener:OnSelectChangeListener?=null
    fun registerSelectChangeListener(selectChangeListener: OnSelectChangeListener){
        onSelectChangeListener = selectChangeListener
    }

    interface OnSelectChangeListener{
        fun onSelectChange(type:Int)
    }

}
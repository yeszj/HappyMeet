package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.adapter.CommonOperateAdapter
import cn.yanhu.commonres.bean.OperateInfo
import cn.yanhu.commonres.databinding.PopCommonOperateBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/10/10
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonOperatePop(
    context: Context,
    val list: MutableList<OperateInfo>,
    val onClickItemListener: OnClickItemListener
) : BottomPopupView(context) {
    private val operateAdapter by lazy { CommonOperateAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_common_operate
    }

    private lateinit var mBiding: PopCommonOperateBinding
    override fun onCreate() {
        super.onCreate()
        mBiding = PopCommonOperateBinding.bind(popupImplView)
        mBiding.recyclerView.adapter = operateAdapter
        operateAdapter.submitList(list)
        operateAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<OperateInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<OperateInfo, *>,
                view: View,
                position: Int
            ) {
                val item = operateAdapter.getItem(position) ?: return
                onClickItemListener.onClickItem(item)
                dismiss()
            }
        })
        mBiding.tvCancel.setOnSingleClickListener { dismiss() }

    }

    interface OnClickItemListener {
        fun onClickItem(operateInfo: OperateInfo)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            list: MutableList<OperateInfo>,
            onClickItemListener: OnClickItemListener
        ): CommonOperatePop {
            val pop = CommonOperatePop(context, list, onClickItemListener)
            val builder = XPopup.Builder(context)
            builder.asCustom(pop).show()
            return pop
        }
    }
}
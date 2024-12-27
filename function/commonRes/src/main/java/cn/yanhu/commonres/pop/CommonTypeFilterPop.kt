package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import cn.yanhu.commonres.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.adapter.CommonTypeFilterAdapter
import cn.yanhu.commonres.bean.FilterInfo
import cn.yanhu.commonres.databinding.PopWalletDetailFilterBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonTypeFilterPop(
    context: Context,
    val list: MutableList<FilterInfo>,
    private val onFilterListener: OnFilterListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_wallet_detail_filter
    }

    private var mBinding: PopWalletDetailFilterBinding? = null
    private var selectPosition = 0
    private val filterAdapter by lazy { CommonTypeFilterAdapter() }
    override fun onCreate() {
        super.onCreate()
        mBinding = PopWalletDetailFilterBinding.bind(popupImplView)
        mBinding?.apply {
            tvCancel.setOnSingleClickListener { dismiss() }
            tvSure.setOnSingleClickListener {
                onFilterListener.onSelectFilter(
                    filterAdapter.getItem(
                        selectPosition
                    )
                )
                dismiss()
            }
            recyclerView.adapter = filterAdapter
            filterAdapter.submitList(list)
            filterAdapter.setOnItemClickListener { _, _, position ->
                selectPosition = position
                filterAdapter.setSelectPosition(position)
            }
        }
    }

    interface OnFilterListener {
        fun onSelectFilter(filterInfo: FilterInfo?)
    }

    companion object {
        fun showPop(
            mContext: Activity, list: MutableList<FilterInfo>, onFilterListener: OnFilterListener
        ): CommonTypeFilterPop {
            val filterPop = CommonTypeFilterPop(mContext, list, onFilterListener)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(filterPop).show()
            return filterPop
        }
    }
}
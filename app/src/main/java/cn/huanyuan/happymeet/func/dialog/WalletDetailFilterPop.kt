package cn.huanyuan.happymeet.func.dialog

import android.app.Activity
import android.content.Context
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.PopWalletDetailFilterBinding
import cn.huanyuan.happymeet.ui.wallet.adapter.WalletFilterAdapter
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.FilterInfo
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class WalletDetailFilterPop(
    context: Context,
    val list: MutableList<FilterInfo>,
    private val onFilterListener: OnFilterListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_wallet_detail_filter
    }

    private var mBinding: PopWalletDetailFilterBinding? = null
    private var selectPosition = 0
    private val filterAdapter by lazy { WalletFilterAdapter() }
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
        ): WalletDetailFilterPop {
            val filterPop = WalletDetailFilterPop(mContext, list, onFilterListener)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(filterPop).show()
            return filterPop
        }
    }
}
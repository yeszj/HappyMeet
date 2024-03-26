package cn.huanyuan.happymeet.ui.userinfo.dressUp

import android.os.Bundle
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgDressUpBinding
import cn.huanyuan.happymeet.func.dialog.DressUpOperatePop
import cn.huanyuan.happymeet.ui.userinfo.dressUp.adapter.MyDressUpItemAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.parseState
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class MyDressUpFrg : BaseFragment<FrgDressUpBinding, DressUpViewModel>(
    R.layout.frg_dress_up, DressUpViewModel::class.java
) {
    private val dressUpItemAdapter by lazy { MyDressUpItemAdapter() }
    private var type = TYPE_AVATAR_FRAME
    private var roseBalance: String = "0"
    override fun initData() {
        type = requireArguments().getInt(IntentKeyConfig.TYPE, TYPE_AVATAR_FRAME)
        val emptyView = getEmptyView()
        emptyView.setFootText("暂未拥有任何装扮")
        dressUpItemAdapter.stateView = emptyView
        mBinding.recyclerView.adapter = dressUpItemAdapter
        requestData()
    }


    override fun requestData() {
        super.requestData()
        mViewModel.getMyDressUpInfo(type)
    }

    override fun lazyLoad() {

    }

    override fun initListener() {
        super.initListener()
        dressUpItemAdapter.setOnItemClickListener { _, _, position ->
            DressUpOperatePop.showDialog(
                mContext, dressUpItemAdapter.getItem(position)!!, type, roseBalance
            )
        }
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refreshLayout)
                }

            })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.myDressInfoObservable.observe(this) { it ->
            parseState(it, {
                roseBalance = it.roseBalance
                dressUpItemAdapter.isStateViewEnable = it.list.size <= 0
                dressUpItemAdapter.submitList(it.list)
            })
        }
        LiveEventBus.get<Int>(LiveDataEventManager.DRESS_UP_SUCCESS).observe(this) { buyId: Int ->
            val item = dressUpItemAdapter.items.firstOrNull {
                it.id == buyId
            }
            val indexOf = dressUpItemAdapter.items.indexOf(item)
            dressUpItemAdapter.notifyItemChanged(indexOf, true)
        }
        LiveEventBus.get<Int>(LiveDataEventManager.DRESS_BUY_SUCCESS).observe(this) {
            if (isRealVisible()) {
                requestData()
            }
        }
    }

    companion object {
        const val TYPE_AVATAR_FRAME = 1
        const val TYPE_CAR = 2
        const val TYPE_CHAT_POP = 3
        const val TYPE_BEAUTIFUL_ACCOUNT = 4
        fun newInstance(type: Int): MyDressUpFrg {
            val dressUpFrg = MyDressUpFrg()
            val bundle = Bundle()
            bundle.putInt(IntentKeyConfig.TYPE, type)
            dressUpFrg.arguments = bundle
            return dressUpFrg
        }
    }
}
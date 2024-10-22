package cn.huanyuan.sweetlove.ui.userinfo.dressUp

import android.os.Bundle
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgDressUpBinding
import cn.huanyuan.sweetlove.func.dialog.DressGoodsBuyPop
import cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter.DressUpItemAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.parseState
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class DressUpFrg : BaseFragment<FrgDressUpBinding, DressUpViewModel>(
    R.layout.frg_dress_up,
    DressUpViewModel::class.java
) {
    private val dressUpItemAdapter by lazy { DressUpItemAdapter() }
    private var type = TYPE_AVATAR_FRAME
    private var roseBalance:String = "0"
    override fun initData() {
        type = requireArguments().getInt(IntentKeyConfig.TYPE, TYPE_AVATAR_FRAME)
        mBinding.recyclerView.adapter = dressUpItemAdapter
        requestData()
    }

    override fun lazyLoad() {

    }


    override fun requestData() {
        super.requestData()
        mViewModel.getDressUpInfo(type)
    }

    override fun initListener() {
        super.initListener()
        dressUpItemAdapter.setOnItemClickListener { _, _, position ->
            DressGoodsBuyPop.showDialog(
                mContext,
                dressUpItemAdapter.getItem(position)!!,type,roseBalance
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
        mViewModel.dressInfoObservable.observe(this) { it ->
            parseState(it, {
                roseBalance = it.roseBalance
                dressUpItemAdapter.submitList(it.list)
            })
        }
        LiveEventBus.get<String>(LiveDataEventManager.ROSE_BALANCE_CHANGE).observe(this){
            //更新玫瑰余额
            roseBalance = it
        }
        LiveEventBus.get<Int>(LiveDataEventManager.DRESS_BUY_SUCCESS).observe(this){ buyId: Int ->
            //购买成功 更新列表中数据为已购买
            val item = dressUpItemAdapter.items.firstOrNull {
                it.id == buyId
            }
            val indexOf = dressUpItemAdapter.items.indexOf(item)
            item?.isHave = true
            dressUpItemAdapter.notifyItemChanged(indexOf)
        }
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                requestData()
            }
        })
    }

    companion object {
        const val TYPE_AVATAR_FRAME = 1
        const val TYPE_CAR = 2
        const val TYPE_CHAT_POP = 3
        const val TYPE_BEAUTIFUL_ACCOUNT = 4
        fun newInstance(type: Int): DressUpFrg {
            val dressUpFrg = DressUpFrg()
            val bundle = Bundle()
            bundle.putInt(IntentKeyConfig.TYPE, type)
            dressUpFrg.arguments = bundle
            return dressUpFrg
        }
    }
}
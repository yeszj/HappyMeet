package cn.huanyuan.sweetlove.ui.userinfo.dressUp

import android.os.Bundle
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgDressUpBinding
import cn.huanyuan.sweetlove.func.dialog.DressUpOperatePop
import cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter.MyDressUpItemAdapter
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


    override fun initListener() {
        super.initListener()
        dressUpItemAdapter.setOnItemClickListener { _, _, position ->
            DressUpOperatePop.showDialog(
                mContext, dressUpItemAdapter.getItem(position)!!, type, roseBalance,object : DressUpOperatePop.OnDressUpListener{
                    override fun onDressUpSuccess(id: Int) {
                        val upIndex = dressUpItemAdapter.items.indexOfFirst {
                            it.isWear && it.id != id
                        }
                        if(upIndex>=0){
                            dressUpItemAdapter.items[upIndex].isWear = false
                            dressUpItemAdapter.notifyItemChanged(upIndex, true)
                        }

                        val indexOf = dressUpItemAdapter.items.indexOfFirst {
                            it.id == id
                        }
                        dressUpItemAdapter.notifyItemChanged(indexOf, true)
                    }

                }
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
                dressUpItemAdapter.isStateViewEnable = it.getGoodsList().size <= 0
                dressUpItemAdapter.submitList(it.getGoodsList())
            })
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
        const val TYPE_USER_FLOAT = 5
        fun newInstance(type: Int): MyDressUpFrg {
            val dressUpFrg = MyDressUpFrg()
            val bundle = Bundle()
            bundle.putInt(IntentKeyConfig.TYPE, type)
            dressUpFrg.arguments = bundle
            return dressUpFrg
        }
    }
}
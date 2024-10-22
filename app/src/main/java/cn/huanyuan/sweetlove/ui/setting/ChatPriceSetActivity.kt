package cn.huanyuan.sweetlove.ui.setting

import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityChatPriceSetBinding
import cn.yanhu.imchat.adapter.ChatPriceSetAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.commonres.pop.CommonWheelViewPop
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class ChatPriceSetActivity : BaseActivity<ActivityChatPriceSetBinding, SettingViewModel>(
    R.layout.activity_chat_price_set,
    SettingViewModel::class.java
) {
    private val priceSetAdapter by lazy { ChatPriceSetAdapter() }
    override fun initData() {
        setFullScreenStatusBar(false)
        setStatusBarStyle(false)
        mBinding.recyclerView.adapter = priceSetAdapter
        priceSetAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<ChatPriceItemInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<ChatPriceItemInfo, *>,
                view: View,
                position: Int
            ) {
                val item = priceSetAdapter.getItem(position) ?: return
                showSelectPricePop(item, position)
            }

        })
        requestData()
    }

    private fun showSelectPricePop(item: ChatPriceItemInfo, position: Int) {
        val list = mutableListOf<String>()
        var selectValue = ""
        item.list.forEach {
            list.add(it.desc)
            if (it.selected) {
                selectValue = it.desc
            }
        }
        CommonWheelViewPop.showDialog(
            mContext,
            list,
            selectValue,
            object : CommonWheelViewPop.OnSelectWheelListener {
                override fun onSelectValue(value: String) {
                    var selectId:Int = 0
                    item.list.forEach {
                        if (it.desc == selectValue) {
                            it.selected = false
                        } else if (it.desc == value) {
                            it.selected = true
                            selectId = it.id
                        }
                    }
                    mViewModel.setPrice(0,item.type,selectId)
                    priceSetAdapter.notifyItemChanged(position)
                }
            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getPriceConfig()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.priceConfigObservable.observe(this) { it ->
            parseState(it, {
                priceSetAdapter.submitList(it)
            })
        }
    }
}
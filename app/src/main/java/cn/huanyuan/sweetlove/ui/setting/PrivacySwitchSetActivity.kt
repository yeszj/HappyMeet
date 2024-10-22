package cn.huanyuan.sweetlove.ui.setting

import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.SwitchConfigInfo
import cn.huanyuan.sweetlove.databinding.ActivityPrivacySwitchSetBinding
import cn.huanyuan.sweetlove.ui.setting.adapter.PrivacySwitchSetAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/9
 * desc:
 */
class PrivacySwitchSetActivity : BaseActivity<ActivityPrivacySwitchSetBinding, SettingViewModel>(
    R.layout.activity_privacy_switch_set,
    SettingViewModel::class.java
) {
    private val switchAdapter by lazy { PrivacySwitchSetAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        mBinding.recyclerView.adapter = switchAdapter
        switchAdapter.addOnItemChildClickListener(R.id.toggle_switch,object : BaseQuickAdapter.OnItemChildClickListener<SwitchConfigInfo>{
            override fun onItemClick(
                adapter: BaseQuickAdapter<SwitchConfigInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position) ?: return
                item.isOpen = !item.isOpen
                switchAdapter.notifyItemChanged(position,true)
            }
        })
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getSwitchSetInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.switchInfoObservable.observe(this){ it ->
            parseState(it,{
                switchAdapter.submitList(it)
            })
        }
    }

}
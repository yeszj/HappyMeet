package cn.huanyuan.sweetlove.ui.system

import android.content.Context
import android.content.Intent
import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivitySystemMessageBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.commonres.bean.SystemMessageInfo
import cn.huanyuan.sweetlove.ui.system.adapter.SystemMessageAdapter
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
@Route(path  = RouterPath.ROUTER_SYSTEMMESSAGE)
class SystemMessageActivity : BaseActivity<ActivitySystemMessageBinding, SystemViewModel>(
    R.layout.activity_system_message,
    SystemViewModel::class.java
) {
    private val msgAdapter by lazy { SystemMessageAdapter() }
    override fun initData() {
        setFullScreenStatusBar(false)
        setStatusBarStyle(false)
        msgAdapter.stateView = getEmptyView()
        mBinding.systemMsgRv.adapter = msgAdapter
        msgAdapter.addOnItemChildClickListener(R.id.btn_read,
            object : BaseQuickAdapter.OnItemChildClickListener<SystemMessageInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<SystemMessageInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = msgAdapter.getItem(position) ?: return
                    PageIntentUtil.url2Page(mContext, item.url)
                }

            })
        mViewModel.readSystemMsg()
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getAllSystemMsg()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.msgInfoObservable.observe(this){ it ->
            parseState(it,{
                msgAdapter.isStateViewEnable = it.size<=0
                msgAdapter.submitList(it)
            })
        }
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context,SystemMessageActivity::class.java))
        }
    }
}
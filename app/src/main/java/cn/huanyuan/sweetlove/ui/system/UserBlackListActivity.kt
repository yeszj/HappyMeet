package cn.huanyuan.sweetlove.ui.system

import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivityUserBlackListBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.ui.system.adapter.BlackUserAdapter
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class UserBlackListActivity : BaseActivity<ActivityUserBlackListBinding, SystemViewModel>(
    R.layout.activity_user_black_list,
    SystemViewModel::class.java
) {
    private val blackUserAdapter by lazy { BlackUserAdapter() }
    override fun initData() {
        blackUserAdapter.stateView = getEmptyView()
        mBinding.recyclerView.adapter = blackUserAdapter
        blackUserAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<BaseUserInfo>{
            override fun onClick(
                adapter: BaseQuickAdapter<BaseUserInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position)?:return
                RouteIntent.lunchPersonHomePage(item.userId)
            }
        })
        blackUserAdapter.addOnItemChildClickListener(R.id.tv_remove,object : BaseQuickAdapter.OnItemChildClickListener<BaseUserInfo>{
            override fun onItemClick(
                adapter: BaseQuickAdapter<BaseUserInfo, *>,
                view: View,
                position: Int
            ) {
                val item = blackUserAdapter.getItem(position)?:return
                ImUserManager.setUserBlack(false,item.userId)
                blackUserAdapter.removeAt(position)
            }
        })
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getBlackUserList()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.blackUserListObservable.observe(this){ it ->
            parseState(it,{
                blackUserAdapter.isStateViewEnable = it.size<=0
                blackUserAdapter.submitList(it)
            })
        }
    }

}
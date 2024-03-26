package cn.yanhu.imchat.conversation

import android.view.View
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.RecommendGroupAdapter
import cn.yanhu.imchat.databinding.FrgGroupConversationBinding
import cn.zj.netrequest.ext.parseState
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class GroupConversationFrg : BaseFragment<FrgGroupConversationBinding, ImChatViewModel>(
    R.layout.frg_group_conversation,
    ImChatViewModel::class.java
) {
    private var page = 1
    private val recommendGroupAdapter by lazy {
        RecommendGroupAdapter()
    }
    override fun initData() {
        addFragment(ImConversationFragment.newsInstance(ImConversationFragment.TYPE_GROUP))
        mBinding.recyclerView.adapter = recommendGroupAdapter
        requestData()
    }

    override fun lazyLoad() {

    }

    override fun initListener() {
        super.initListener()
        recommendGroupAdapter.setOnItemClickListener { _, _, position ->
            val item = recommendGroupAdapter.getItem(position)
            RouteIntent.lunchGroupDetail(item?.groupId)
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.groupListObserver.observe(this){ it ->
            parseState(it,{
                if (page==1){
                    recommendGroupAdapter.submitList(it)
                }else{
                    recommendGroupAdapter.addAll(it)
                }
                setDataLoadFinish(page,it.size,mBinding.refresh)
            },{
                endLoad(page,mBinding.refresh)
            })
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.CHAT_GROUP_MSG).observe(this){
            if (it){
                mBinding.tvHasJoin.visibility = View.GONE
            }else{
                mBinding.tvHasJoin.visibility = View.VISIBLE
            }
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRecommendGroupList(page)
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance().initRefresh(context,true,mBinding.refresh,object : IRefreshCallBack{
            override fun onRefresh() {
                page = 1
                requestData()
            }

            override fun onLoadMore() {
                page ++
                requestData()
            }
        })
    }

}
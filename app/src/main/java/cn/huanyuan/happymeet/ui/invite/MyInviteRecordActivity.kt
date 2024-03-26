package cn.huanyuan.happymeet.ui.invite

import android.content.Context
import android.content.Intent
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityMyInviteRecordBinding
import cn.huanyuan.happymeet.ui.invite.adapter.InviteRecordAdapter
import cn.huanyuan.happymeet.ui.invite.adapter.InviteRecordHeadAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.QuickAdapterHelper

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:我的邀请记录
 */
class MyInviteRecordActivity : BaseActivity<ActivityMyInviteRecordBinding, InviteViewModel>(
    R.layout.activity_my_invite_record,
    InviteViewModel::class.java
) {
    private val recordHeadAdapter by lazy { InviteRecordHeadAdapter() }
    private val recordAdapter by lazy { InviteRecordAdapter() }
    private lateinit var helper: QuickAdapterHelper
    private var page = 1
    override fun initData() {
        setFullScreenStatusBar()
        helper = QuickAdapterHelper.Builder(recordAdapter)
            .build()
        mBinding.rvRecord.adapter = helper.adapter
        helper.addBeforeAdapter(recordHeadAdapter)
        requestData()
    }

    override fun initListener() {
        super.initListener()
        recordAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            RouteIntent.lunchPersonHomePage(item?.userId)
        }
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, true, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    page = 1
                    requestData()
                }

                override fun onLoadMore() {
                    page++
                    requestData()
                }
            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getMyInviteUser(page)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.myInviteInfoObservable.observe(this) { it ->
            parseState(it, {
                val list = it.list
                if (page == 1) {
                    recordHeadAdapter.item = it
                    recordAdapter.submitList(list)
                } else {
                    recordAdapter.addAll(list)
                }
                setDataLoadFinish(page, list.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context, MyInviteRecordActivity::class.java))
        }
    }
}
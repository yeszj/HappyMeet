package cn.huanyuan.sweetlove.ui.userinfo.level

import android.R.attr.resource
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityUserLevelBinding
import cn.huanyuan.sweetlove.func.dialog.UserLevelRulePop
import cn.huanyuan.sweetlove.func.dialog.UserPrivilegeExamplePop
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.huanyuan.sweetlove.ui.userinfo.adapter.UserLevelHeadAdapter
import cn.huanyuan.sweetlove.ui.userinfo.adapter.UserLevelPrivilegeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.view.TitleBar
import cn.zj.netrequest.ext.parseState
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter4.QuickAdapterHelper

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class UserLevelActivity : BaseActivity<ActivityUserLevelBinding, UserViewModel>(
    R.layout.activity_user_level,
    UserViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private val headAdapter by lazy { UserLevelHeadAdapter() }
    private val privilegeAdapter by lazy { UserLevelPrivilegeAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        helper = QuickAdapterHelper.Builder(privilegeAdapter)
            .build()
        privilegeAdapter.setOnItemClickListener { adapter, _, position ->
            if (position!=adapter.itemCount-1){
                val item = adapter.getItem(position)
                UserPrivilegeExamplePop.showDialog(mContext, item!!)
            }
        }
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(0, headAdapter.apply {
        })
        requestData()
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

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                showLevelRulePop()
            }

        })
    }

    private var userLevelRulePop:UserLevelRulePop?=null
    private fun showLevelRulePop() {
        if (CommonUtils.isPopShow(userLevelRulePop)){
            return
        }
        if (wealthDrawable != null && ruleDrawable != null) {
            userLevelRulePop = UserLevelRulePop.showDialog(mContext, ruleDrawable!!, wealthDrawable!!)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getUserLevelInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.userLevelObservable.observe(this) { it ->
            parseState(it, {
                headAdapter.item = it
                val ruleInfo = it!!.ruleInfo
                loadRuleImg(ruleInfo.levelRuleImg)
                loadWealthImg(ruleInfo.wealthSourceImg)
                privilegeAdapter.submitList(it.privilegeList)
            })
        }
    }

    private var ruleDrawable:Drawable?=null
    private fun loadRuleImg(img:String){
        GlideUtils.loadAsDrawable(mContext,img){
            ruleDrawable = it
        }
    }

    private var wealthDrawable:Drawable?=null
    private fun loadWealthImg(img:String){
        GlideUtils.loadAsDrawable(mContext,img){
            wealthDrawable = it
        }
    }

    companion object {
        fun lunch(mContext: Context) {
            mContext.startActivity(Intent(mContext,UserLevelActivity::class.java))
        }
    }
}
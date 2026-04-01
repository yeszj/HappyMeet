package cn.huanyuan.sweetlove.ui.invite

import android.content.Context
import android.content.Intent
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.InviteUserDataRes
import cn.huanyuan.sweetlove.databinding.ActivityInviteUserDataDetailBinding
import cn.huanyuan.sweetlove.ui.invite.adapter.InviteUserTotalAdapter
import cn.yanhu.agora.adapter.statistic.LiveStatisticsTotalAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState
import  cn.yanhu.baselib.utils.TypefaceUtils

/**
 * @author: zhengjun
 * created: 2026/3/23
 * desc:
 */
class InviteUserDataDetailActivity :
    BaseActivity<ActivityInviteUserDataDetailBinding, InviteViewModel>(
        R.layout.activity_invite_user_data_detail, InviteViewModel::class.java
    ) {

    var inviteUserId: String = ""
    private val normalAdapter by lazy { InviteUserTotalAdapter() }
    private val dateTotalAdapter by lazy { InviteUserTotalAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        inviteUserId = intent.getStringExtra(IntentKeyConfig.ID).toString()
        mBinding.rvLive.adapter = normalAdapter
        mBinding.rvInviteTotal.adapter = dateTotalAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getMyInviteUserDetail(inviteUserId)
    }

    private var isMonth: Boolean = true
    override fun initListener() {
        super.initListener()
        mBinding.tvMonth.setOnSingleClickListener {
            switchToMonth()
        }
        mBinding.tvWeek.setOnSingleClickListener {
            switchToWeek()
        }
    }

    private fun switchToWeek() {
        if (isMonth) {
            isMonth = false
            mBinding.tvMonth.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.color_888888))
            mBinding.tvMonth.typeface = TypefaceUtils.getTypeface(400, mContext)
            mBinding.tvWeek.typeface = TypefaceUtils.getTypeface(500, mContext)
            mBinding.tvWeek.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor))
            dateTotalAdapter.submitList(inviteDataInfo?.weekData)
        }
    }

    private fun switchToMonth() {
        if (!isMonth) {
            isMonth = true
            mBinding.tvMonth.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor))
            mBinding.tvMonth.typeface = TypefaceUtils.getTypeface(500, mContext)
            //TextFontStyleUtils.setTextFontStyle(mBinding.tvMonth,)
            mBinding.tvWeek.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.color_888888))
            mBinding.tvWeek.typeface = TypefaceUtils.getTypeface(400, mContext)
            dateTotalAdapter.submitList(inviteDataInfo?.monthData)
        }
    }

    private var inviteDataInfo: InviteUserDataRes? = null
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.nyInviteUserDataObservable.observe(this) { resultState ->
            parseState(resultState, {
                inviteDataInfo = it
                mBinding.userinfo = it.userInfo
                normalAdapter.submitList(it.normalData)
                dateTotalAdapter.submitList(it.monthData)
            })
        }
    }
    companion object{
        fun lunch(context: Context, inviteUserId: String){
            val intent = Intent(context, InviteUserDataDetailActivity::class.java)
            intent.putExtra(IntentKeyConfig.ID, inviteUserId)
            context.startActivity(intent)
        }
    }
}
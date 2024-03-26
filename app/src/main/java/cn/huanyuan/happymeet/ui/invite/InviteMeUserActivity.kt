package cn.huanyuan.happymeet.ui.invite

import android.content.Context
import android.content.Intent
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityInviteMeUserBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:邀请我的人
 */
class InviteMeUserActivity : BaseActivity<ActivityInviteMeUserBinding, InviteViewModel>(
    R.layout.activity_invite_me_user,
    InviteViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        requestData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.viewBg.setOnSingleClickListener {
            RouteIntent.lunchPersonHomePage(mBinding.userinfo?.userId)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getInviteMyUser()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.userInfoObservable.observe(this){ it ->
            parseState(it,{
                mBinding.userinfo = it
            })
        }
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context, InviteMeUserActivity::class.java))
        }
    }
}
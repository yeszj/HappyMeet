package cn.yanhu.imchat.ui.chatSetting

import android.content.Context
import android.content.Intent
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.databinding.ActivityUserChatSettingBinding
import cn.yanhu.imchat.manager.ImUserManager


/**
 * @author: zhengjun
 * created: 2024/3/27
 * desc:
 */
@Suppress("DEPRECATION")
class UserChatSettingActivity : BaseActivity<ActivityUserChatSettingBinding, ImChatViewModel>(
    R.layout.activity_user_chat_setting,
    ImChatViewModel::class.java
) {
    private var chatUserId:String = ""
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        val userInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as UserDetailInfo?
        userInfo?.apply {
            chatUserId = this.userId
            setUserInfo()
            setUseBlack()
            setMessageNotify()
            setStickTop()
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.bgUser.setOnSingleClickListener {
            RouteIntent.lunchPersonHomePage(chatUserId)
        }
        mBinding.tvReport.setOnSingleClickListener {
            RouteIntent.lunchReportPage()
        }
    }

    private fun UserDetailInfo.setUserInfo() {
        GlideUtils.load(mContext, this.portrait, mBinding.ivAvatar)
        mBinding.tvNickName.text = this.nickName
    }

    private fun UserDetailInfo.setUseBlack() {
        val inBlack = ImUserManager.isInBlack(this.userId)
        mBinding.toggleBlack.isChecked = inBlack
        mBinding.toggleBlack.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setUserBlack(isChecked, this.userId)
        }
    }

    private fun UserDetailInfo.setMessageNotify() {
        val needMessageNotify = ImUserManager.isNeedMessageNotify(this.userId)
        mBinding.toggleMute.isChecked = !needMessageNotify
        mBinding.toggleMute.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setNotify(
                isChecked,
                this@setMessageNotify.userId
            )
        }
    }

    private fun UserDetailInfo.setStickTop() {
        val stickTop = ImUserManager.isStickTop(this.userId)
        mBinding.toggleStickTop.isChecked = stickTop
        mBinding.toggleStickTop.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setStickTop(
                isChecked,
                this@setStickTop.userId
            )
        }
    }

    companion object{
       fun lunch(context: Context,userInfo:UserDetailInfo?){
            val intent = Intent(context,UserChatSettingActivity::class.java)
           intent.putExtra(IntentKeyConfig.DATA,userInfo)
           context.startActivity(intent)
        }
    }
}
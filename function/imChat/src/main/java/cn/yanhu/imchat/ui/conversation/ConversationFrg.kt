package cn.yanhu.imchat.ui.conversation

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cn.yanhu.baselib.adapter.MyFrgFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.callBack.OnSingleClickListener
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.commonres.manager.AppManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.databinding.FrgConversationBinding
import cn.yanhu.imchat.databinding.ViewImListTopBinding
import cn.zj.netrequest.ext.parseState
import com.permissionx.guolindev.PermissionX
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class ConversationFrg : BaseFragment<FrgConversationBinding, ImChatViewModel>(
    R.layout.frg_conversation,
    ImChatViewModel::class.java
) {
    private lateinit var topBinding: ViewImListTopBinding
    override fun initData() {
        addTopMsgView()
        initTabLayout()
        initVpData()
        verifyPermission()
        requestData()
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("全部消息")
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.viewPager,
            list.toTypedArray(),
            selectColor = cn.yanhu.baselib.R.color.fontTextColor,
            normalColor = cn.yanhu.baselib.R.color.fontGrayColor,
            lineColor = cn.yanhu.baselib.R.color.transparent,
            lineWidth = 0f,
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8)
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.viewPager)
    }



    private var frgList: ArrayList<Fragment> = arrayListOf()
    private fun initVpData() {
        val imConversationListFrg = IMConversationListFrg.newsInstance(false, IMConversationListFrg.TYPE_ALL)
        frgList.add(imConversationListFrg)

       // frgList.add(ImConversationFragment.newsInstance(ImConversationFragment.TYPE_SINGLE_INTIMATE))

        mBinding.viewPager.adapter = MyFrgFragmentStateAdapter(this@ConversationFrg, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
    }


    override fun requestData() {
        super.requestData()
        mViewModel.getSystemMsg()
    }

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.systemMsgObserver.observe(this){ it ->
            parseState(it,{
                val unReadCount = it.unReadSystemCount

                if (unReadCount > 0) {
                    topBinding.tvUnReadSystemMsg.visibility = View.VISIBLE
                    if (unReadCount > 99) {
                        topBinding.tvUnReadSystemMsg.text = "99+"
                    } else {
                        topBinding.tvUnReadSystemMsg.text = unReadCount.toString()
                    }
                } else {
                    topBinding.tvUnReadSystemMsg.visibility = View.INVISIBLE
                }
                val systemNotice = it.systemNotice
                if (systemNotice!=null){
                    topBinding.imSystemMsgTime.text = systemNotice.time
                    topBinding.imSystemMsgContent.text = systemNotice.content
                }else{
                    topBinding.imSystemMsgTime.text = ""
                    topBinding.imSystemMsgContent.text = "暂无官方消息"
                }
            })
        }
    }

    //验证权限
    private fun verifyPermission() {
        if (!NotificationManagerCompat.from(mContext).areNotificationsEnabled()) {
            topBinding.noticeRl.visibility = View.VISIBLE
            topBinding.permissionTxt.text = "打开推送通知，以免错过新消息"
            topBinding.noticeApplyPermission.setOnClickListener { isRequestPermission() } //申请通知权限
        } else {
            val isFloatPermission = Settings.canDrawOverlays(context)
            if (!isFloatPermission) {
                topBinding.noticeRl.visibility = View.VISIBLE
                topBinding.permissionTxt.text = "打开悬浮窗，以免错过重要消息"
                topBinding.noticeApplyPermission.setOnClickListener { isFloatPermission() } //申请悬浮窗
            }
        }
        topBinding.noticeCancel.setOnSingleClickListener(object : OnSingleClickListener{
            override fun onSingleClick(v: View?) {
                topBinding.noticeRl.visibility = View.GONE
            }
        })
    }
    private fun addTopMsgView(){
        val imInflater = LayoutInflater.from(mContext).inflate(R.layout.view_im_list_top, null)
        mBinding.llRoot.addView(imInflater, 0)
        topBinding = DataBindingUtil.bind(imInflater)!!
        topBinding.browserCl.setOnSingleClickListener {
            RouteIntent.lunchSeenMeHistory()
            View.INVISIBLE.also { topBinding.tvReadCount.visibility = it }
        }
        topBinding.notifyRl.setOnSingleClickListener {
            RouteIntent.lunchSystemMsgPage()
            View.INVISIBLE.also { topBinding.tvUnReadSystemMsg.visibility = it }
        }
    }

    //判断是否授权必要权限
    private fun isRequestPermission() {
        val permissions = java.util.ArrayList<String>()
        permissions.add(PermissionX.permission.POST_NOTIFICATIONS)
        PermissionXUtils.checkPermission(
            mContext,
            permissions,
            "为了让您及时收到消息通知，请先同意推送和通知的权限",
            "您拒绝授权权限，将无法体验部分功能",
            object : PermissionXUtils.PermissionListener {
                override  fun onSuccess() {
                    AppManager.setAppState(
                        AppManager.STATE_FOREGROUND,
                        PermissionX.areNotificationsEnabled(mContext)
                    )
                    showToast("已打开通知权限")
                    topBinding.noticeRl.visibility = View.GONE
                    verifyPermission()
                }

                override fun onFail() {}
            })
    }

    private fun isFloatPermission() {
        PermissionXUtils.checkAlertPermission(activity,"退出", object :
            PermissionXUtils.OnAlertPermissionListener {
           override fun onSuccess() {
                showToast("已打开悬浮窗权限")
               topBinding.noticeRl.visibility = View.GONE
            }
            override fun onFail() {}
            override fun onClose() {}
        })
    }
}
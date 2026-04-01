package cn.huanyuan.sweetlove.ui.event.cp

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.widget.AppCompatImageView
import cn.huanyuan.sweetlove.ui.event.EventViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.CommonEventImgConfig
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.BaseBean
import androidx.core.graphics.toColorInt
import cn.huanyuan.sweetlove.bean.HeartCpEventRankResponse
import cn.huanyuan.sweetlove.bean.HeartCpSignResponse
import cn.huanyuan.sweetlove.databinding.ActivityHeartCpEventBinding
import cn.huanyuan.sweetlove.func.dialog.HeartCpSignPop
import cn.huanyuan.sweetlove.func.dialog.HeartCpSignPop.OnHeartCpClickListener
import cn.huanyuan.sweetlove.func.dialog.HeartCpUserInvitePop
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.HeartCpUserInfo
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.manager.ImageThumbUtils
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.parseState2
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ErrorCode
import cn.zj.netrequest.status.ResultState
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback


/**
 * @author: zhengjun
 * created: 2025/1/17
 * desc:
 */
class HeartCpEventActivity : BaseActivity<ActivityHeartCpEventBinding, EventViewModel>(
    R.layout.activity_heart_cp_event,
    EventViewModel::class.java
) {
    private val rankAdapter by lazy { HeartCpEventRankAdapter() }
    private var isLeftTab = true
    private var tab1Drawable: Drawable? = null
    private var tab2Drawable: Drawable? = null
    private var charmRankRes: HeartCpEventRankResponse? = null
    private var contributeRankRes: HeartCpEventRankResponse? = null

    private var isFirstLoad = true
    private var activityId = ""
    override fun initData() {
        activityId = intent.getStringExtra(IntentKeyConfig.ACTIVITY_ID).toString()
        setFullScreenStatusBar()
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无榜单")
        rankAdapter.stateView = emptyView
        mBinding.rvRank.adapter = rankAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getHeartCpEventConfig(activityId)
        getEventRank(TYPE_CHARM)
        getEventRank(TYPE_CONTRIBUTE, true)
        preloadSignInCalendar()
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    refreshData()
                }
            })
    }

    private fun refreshData() {
        mViewModel.getHeartCpEventConfig(activityId)
        if (isLeftTab) {
            getEventRank(TYPE_CHARM)
        } else {
            getEventRank(TYPE_CONTRIBUTE)
        }
        preloadSignInCalendar()
    }


    private fun getEventRank(type: Int, isPreload: Boolean = false) {
        mViewModel.getHeartCpRank(
            type,
            activityId,
            object : OnRequestResultListener<HeartCpEventRankResponse> {
                override fun onSuccess(data: BaseBean<HeartCpEventRankResponse>) {
                    if (type == 0) {
                        charmRankRes = data.data ?: return
                        bindRank(charmRankRes!!)
                        bindMyInfo(charmRankRes!!)
                    } else {
                        contributeRankRes = data.data ?: return
                        if (isPreload) {
                            return
                        }
                        bindRank(contributeRankRes!!)
                    }
                    if (isFirstLoad) {
                        isFirstLoad = false
                        bindRankInfo()
                    }
                }
            })
    }

    private fun bindRank(commonRankRes: HeartCpEventRankResponse) {
        val rankList = commonRankRes.list
        rankAdapter.setEventRankInfo(commonRankRes)


        rankAdapter.isStateViewEnable = rankList.isEmpty()
        rankAdapter.submitList(rankList)
    }

    private fun bindMyInfo(commonRankRes: HeartCpEventRankResponse) {
        val myRank = commonRankRes.myRank
        mBinding.rankInfo = myRank
        val topThreeIcon = commonRankRes.topThreeIcon
        val rankingNum = myRank?.rankNum
        if (myRank == null) {
            CoilImgUtils.loadCircleImg(
                ImageThumbUtils.getThumbUrl(ImUserManager.getSelfUserInfo().portrait),
                mBinding.userAvatarA
            )
            mBinding.userAvatarB.setImageResource(R.mipmap.icon_avatar_heart_default)
        }
        if (!TextUtils.isEmpty(rankingNum)) {
            CoilImgUtils.loadCircleImg(myRank?.portraitA, mBinding.userAvatarA)
            CoilImgUtils.loadCircleImg(myRank?.portraitB, mBinding.userAvatarB)

            when (rankingNum) {
                "1" -> {
                    loadRankDrawable(topThreeIcon.one)
                }

                "2" -> {
                    loadRankDrawable(topThreeIcon.two)
                }

                "3" -> {
                    loadRankDrawable(topThreeIcon.three)
                }

                else -> {
                    mBinding.tvNum.text = rankingNum
                    mBinding.vgRank.visibility = View.GONE
                    mBinding.tvNum.visibility = View.VISIBLE
                }
            }
        } else {
            mBinding.tvNum.text = "-"
            mBinding.vgRank.visibility = View.GONE
            mBinding.tvNum.visibility = View.VISIBLE
        }
    }

    private fun loadRankDrawable(rankIcon: String) {
        mBinding.vgRank.visibility = View.VISIBLE
        mBinding.tvNum.visibility = View.GONE
        GlideUtils.loadAsDrawable(mContext, rankIcon) {
            mBinding.ivRank.setImageDrawable(it)
        }
    }

    private fun bindRankInfo() {
        charmRankRes?.apply {
            GlideUtils.loadAsDrawable(mContext, this.bgImg) {
                mBinding.ivRankBg.setImageDrawable(it)
            }
            preloadTab1(false)
            preloadTab2(true)

        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvUnBind.setOnSingleClickListener {
            DialogUtils.showConfirmDialog("解绑提醒", {
                mViewModel.unBindCp(activityId)
            }, {

            }, "解绑后，榜单将失效且亲密值将不再计算，确认解绑心动CP吗？", "再想想", "确认解绑")

        }
        mBinding.ivSignIn.setOnSingleClickListener {
            checkSignPop()
        }
        mBinding.tvInviteBind.setOnSingleClickListener {
            showInviteBindCpPop()
        }
        initOnScrollChangeListener()
        mBinding.ivRule.setOnSingleClickListener {
            RouteIntent.lunchToWebView(commonEventImgConfig?.ruleUrl)
        }
        mBinding.leftTab.setOnSingleClickListener {
            showLeftRank()

        }
        mBinding.rightTab.setOnSingleClickListener {
            if (isLeftTab) {
                if (tab2Drawable != null) {
                    mBinding.ivTab.setImageDrawable(tab2Drawable)
                } else {
                    preloadTab2()
                }
                isLeftTab = false
                if (contributeRankRes != null) {
                    bindRank(contributeRankRes!!)
                }
                getEventRank(TYPE_CONTRIBUTE)
            }

        }
    }


    private var heartSingRes: HeartCpSignResponse? = null
    private fun preloadSignInCalendar(isShowPop: Boolean = false) {
        request(
            { rxApi.getSignInCalendar(activityId) },
            object : OnRequestResultListener<HeartCpSignResponse> {
                override fun onSuccess(data: BaseBean<HeartCpSignResponse>) {
                    heartSingRes = data.data ?: return
                    if (isShowPop) {
                        showSignPop()
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    DialogUtils.dismissLoading()
                }
            })
    }

    private var isShowSignPop = false
    private var singPop: HeartCpSignPop? = null
    private fun checkSignPop() {
        if (heartSingRes != null) {
            showSignPop()
        } else {
            preloadSignInCalendar(true)
        }

    }

    private fun showSignPop() {
        isShowSignPop = true
        if (CommonUtils.isPopShow(singPop)) {
            return
        }
        singPop = HeartCpSignPop.showDialog(
            mContext,
            activityId,
            heartSingRes!!,
            object : OnHeartCpClickListener {
                override fun onBindCp() {
                    showInviteBindCpPop()
                }
            },object : SimpleCallback(){
                override fun onDismiss(popupView: BasePopupView?) {
                    refreshData()
                }
            })
    }

    private fun showInviteBindCpPop(): HeartCpUserInvitePop =
        HeartCpUserInvitePop.showDialog(mContext, activityId)

    private fun showLeftRank() {
        if (!isLeftTab) {
            if (tab1Drawable != null) {
                mBinding.ivTab.setImageDrawable(tab1Drawable)
            } else {
                preloadTab1()
            }
            isLeftTab = true
            if (charmRankRes != null) {
                bindRank(charmRankRes!!)
            }
            getEventRank(TYPE_CHARM)
        }
    }

    private fun preloadTab1(isPreload: Boolean = true) {
        if (charmRankRes == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            charmRankRes!!.leftButton
        ) {
            tab1Drawable = it
            if (!isPreload) {
                mBinding.ivTab.setImageDrawable(tab1Drawable)
            }
        }
    }

    private fun preloadTab2(isPreload: Boolean = true) {
        if (charmRankRes == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            charmRankRes!!.rightButton
        ) {
            tab2Drawable = it
            if (!isPreload) {
                mBinding.ivTab.setImageDrawable(tab2Drawable)
            }
        }
    }

    private var commonEventImgConfig: CommonEventImgConfig? = null
    private var myCpInfo: HeartCpUserInfo? = null

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.unBindCpLivedata.observe(this) { it ->
            parseState2(it, {
                unBindSuccess()
            })
        }
        mViewModel.eventConfigLivedata.observe(this) { it ->
            parseState(it, { it ->
                commonEventImgConfig = it
                if (it.needSignIn && !isShowSignPop) {
                    checkSignPop()
                }
                myCpInfo = it.myCp

                mBinding.titleBar.setTitleName(it.eventName)
                mBinding.eventImageConfig = it
                mBinding.vgShadow.setGradientColor(
                    270,
                    it.bgColor.start.toColorInt(),
                    it.bgColor.end.toColorInt()
                )
                mBinding.tvReward.setShadowColor(
                    it.rewardPool.activeNumColor.start.toColorInt(),
                    it.rewardPool.activeNumColor.end.toColorInt()
                )
                mBinding.tvTips.setTextColor(it.copyrightColor.toColorInt())
                mBinding.vgImg.removeAllViews()
                endRefreshing(mBinding.refreshLayout)
                it.eventImgs.forEach {
                    addImageView(it)
                }
            })
        }
    }

    private fun unBindSuccess() {
        showToast("解绑成功")
        val params = HashMap<String, String>()
        params[ImMessageParamsConfig.SENDSHOWCONTENT] =
            "您已解绑心动CP～"
        params[ImMessageParamsConfig.RECEIVESHOWCONTENT] =
            "${ImUserManager.getSelfUserInfo().nickName}已解绑心动CP～"
        myCpInfo?.apply {
            EmMsgManager.sendCommonTipMsg(this.cpUserId, params)
        }
        refreshData()
    }

    private fun initOnScrollChangeListener() {

        mBinding.scrollView.setOnScrollChangeListener(View.OnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY < 0) {
                return@OnScrollChangeListener
            }
            val toolbarHeight: Float = mBinding.titleBar.bottom * 1.5f
            if (scrollY <= toolbarHeight) {
                val scale = scrollY.toFloat() / toolbarHeight
                val alpha = scale * 255
                mBinding.titleBar.setBackgroundColor(
                    Color.argb(
                        alpha.toInt(), 255, 255, 255
                    )
                )
                if (scrollY <= 45) {
                    setStatusBarStyle(true)
                    mBinding.titleBar.setTitleNameColor(R.color.white)
                    mBinding.titleBar.setLeftDrawable(cn.yanhu.baselib.R.drawable.svg_white_back)
                }
            } else {
                setStatusBarStyle(false)
                mBinding.titleBar.setBackgroundColor(Color.WHITE)
                mBinding.titleBar.setTitleNameColor(cn.yanhu.commonres.R.color.cl_common)
                mBinding.titleBar.setLeftDrawable(cn.yanhu.baselib.R.drawable.svg_black_back)
            }
        })
    }


    private fun addImageView(url: String) {
        val imageView = AppCompatImageView(mContext)
        val layoutParams = MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_12)
        imageView.layoutParams = layoutParams
        imageView.adjustViewBounds = true
        GlideUtils.loadAsDrawable(mContext, url) {
            imageView.setImageDrawable(it)
        }
        mBinding.vgImg.addView(imageView)
    }

    companion object {
        const val TYPE_CHARM = 0
        const val TYPE_CONTRIBUTE = 1
    }
}
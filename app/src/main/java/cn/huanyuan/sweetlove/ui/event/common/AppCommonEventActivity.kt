package cn.huanyuan.sweetlove.ui.event.common

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.widget.AppCompatImageView
import cn.huanyuan.sweetlove.databinding.ActivityAppCommonEventBinding
import cn.huanyuan.sweetlove.ui.event.EventViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.CommonEventImgConfig
import cn.huanyuan.sweetlove.bean.CommonEventRankResponse
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.BaseBean
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


/**
 * @author: zhengjun
 * created: 2025/1/17
 * desc:
 */
class AppCommonEventActivity : BaseActivity<ActivityAppCommonEventBinding, EventViewModel>(
    R.layout.activity_app_common_event,
    EventViewModel::class.java
) {
    private val rankAdapter by lazy { CommonEventRankAdapter() }
    private var isLeftTab = true
    private var tab1Drawable: Drawable? = null
    private var tab2Drawable: Drawable? = null
    private var charmRankRes: CommonEventRankResponse? = null
    private var contributeRankRes: CommonEventRankResponse? = null

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
        mViewModel.getCommonEventConfig(activityId)
        getEventRank(TYPE_CHARM)
        getEventRank(TYPE_CONTRIBUTE, true)
    }

    private fun getEventRank(type: Int, isPreload: Boolean = false) {
        mViewModel.getCommonEventRank(
            type,
            activityId,
            object : OnRequestResultListener<CommonEventRankResponse> {
                override fun onSuccess(data: BaseBean<CommonEventRankResponse>) {
                    if (type == 0) {
                        charmRankRes = data.data ?: return
                        bindRank(charmRankRes!!)
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

    private fun bindRank(commonRankRes:CommonEventRankResponse){
        val rankList = commonRankRes.list
        rankAdapter.setEventRankInfo(commonRankRes)

        bindMyInfo(commonRankRes)

        rankAdapter.isStateViewEnable = rankList.isEmpty()
        rankAdapter.submitList(rankList)
    }

    private fun bindMyInfo(commonRankRes: CommonEventRankResponse) {
        val myInfo = commonRankRes.myInfo
        mBinding.rankInfo = myInfo
        val topThreeIcon = commonRankRes.topThreeIcon
        val rankingNum = myInfo.rankingNum
        if (!TextUtils.isEmpty(rankingNum)) {
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
        GlideUtils.loadAsDrawable(mContext, rankIcon, object :
            CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                mBinding.ivRank.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
    }

    private fun bindRankInfo() {
        charmRankRes?.apply {
            val myRankBgColor = this.myInfo
            mBinding.bgMyInfo.setGradientColor(
                270,
                Color.parseColor(myRankBgColor.bgStart),
                Color.parseColor(myRankBgColor.bgEnd)
            )
            GlideUtils.loadAsDrawable(mContext, this.bgImg, object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    mBinding.ivRankBg.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            preloadTab1(false)
            preloadTab2(true)

        }
    }

    override fun initListener() {
        super.initListener()
        initOnScrollChangeListener()
        mBinding.ivRule.setOnSingleClickListener {
            RouteIntent.lunchToWebView(commonEventImgConfig?.ruleUrl)
        }
        mBinding.leftTab.setOnSingleClickListener {
            if (!isLeftTab) {
                if (tab1Drawable != null) {
                    mBinding.ivTab.setImageDrawable(tab1Drawable)
                } else {
                    preloadTab1()
                }
                isLeftTab = true
                if (charmRankRes!=null){
                    bindRank(charmRankRes!!)
                }
                getEventRank(TYPE_CHARM)
            }

        }
        mBinding.rightTab.setOnSingleClickListener {
            if (isLeftTab ) {
                if (tab2Drawable != null) {
                    mBinding.ivTab.setImageDrawable(tab2Drawable)
                } else {
                    preloadTab2()
                }
                isLeftTab = false
                if (contributeRankRes!=null){
                    bindRank(contributeRankRes!!)
                }
                getEventRank(TYPE_CONTRIBUTE)
            }

        }
    }

    private fun preloadTab1(isPreload: Boolean = true) {
        if (charmRankRes == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            charmRankRes!!.leftButton,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    tab1Drawable = resource
                    if (!isPreload) {
                        mBinding.ivTab.setImageDrawable(tab1Drawable)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun preloadTab2(isPreload: Boolean = true) {
        if (charmRankRes == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            charmRankRes!!.rightButton,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    tab2Drawable = resource
                    if (!isPreload) {
                        mBinding.ivTab.setImageDrawable(tab2Drawable)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private var commonEventImgConfig: CommonEventImgConfig? = null
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.eventConfigLivedata.observe(this) { it ->
            parseState(it, { it ->
                commonEventImgConfig = it
                mBinding.titleBar.setTitleName(it.eventName)
                mBinding.eventImageConfig = it
                mBinding.vgShadow.setGradientColor(
                    270,
                    Color.parseColor(it.bgColor.start),
                    Color.parseColor(it.bgColor.end)
                )
                mBinding.tvReward.setShadowColor(
                    Color.parseColor(it.rewardPool.activeNumColor.start),
                    Color.parseColor(it.rewardPool.activeNumColor.end)
                )
                mBinding.tvTips.setTextColor(Color.parseColor(it.copyrightColor))
                mBinding.vgImg.removeAllViews()
                it.eventImgs.forEach {
                    addImageView(it)
                }
            })
        }
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
        GlideUtils.loadAsDrawable(mContext, url, object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                imageView.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
        mBinding.vgImg.addView(imageView)
    }

    companion object {
        const val TYPE_CHARM = 0
        const val TYPE_CONTRIBUTE = 1
    }
}
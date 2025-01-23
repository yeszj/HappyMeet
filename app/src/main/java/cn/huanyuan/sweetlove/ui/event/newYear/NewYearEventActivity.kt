package cn.huanyuan.sweetlove.ui.event.newYear

import android.Manifest
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivityNewYearEventBinding
import cn.huanyuan.sweetlove.ui.event.EventViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.NewYearEventImgConfig
import cn.huanyuan.sweetlove.bean.NewYearInfo
import cn.huanyuan.sweetlove.bean.NewYearRankInfo
import cn.huanyuan.sweetlove.bean.NewYearRankResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.utils.CalendarUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.ServiceConfigKeyManager
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.sdk.share.ContentShare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel

/**
 * @author: zhengjun
 * created: 2025/1/16
 * desc:
 */
class NewYearEventActivity : BaseActivity<ActivityNewYearEventBinding, EventViewModel>(
    R.layout.activity_new_year_event, EventViewModel::class.java
) {
    private val rankAdapter by lazy { NewYearRankAdapter() }
    private var dayRankList: MutableList<NewYearRankInfo> = mutableListOf()
    private var totalRankList: MutableList<NewYearRankInfo> = mutableListOf()
    private var isLeftTab = true
    private var showScaleAnim:AnimatorSet?=null
    override fun initData() {
        setFullScreenStatusBar()
        requestData()
        getImageConfig()
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无榜单")
        rankAdapter.stateView = emptyView
        mBinding.rvRank.adapter = rankAdapter
        getNewYearRank(0)
        getNewYearRank(1,true)
        showScaleAnim = AnimManager.showScaleAnim(mBinding.btnShare, 1.1f)
    }

    private fun getNewYearRank(type: Int,isPreload: Boolean = false) {
        mViewModel.getNewYearRank(type, object : OnRequestResultListener<NewYearRankResponse> {
            override fun onSuccess(data: BaseBean<NewYearRankResponse>) {
                val newYearRankResponse = data.data
                newYearRankResponse?.apply {
                    val rankList = this.list
                    if (type == 0) {
                        dayRankList = rankList
                    } else {
                        totalRankList = rankList
                    }
                    if (isPreload){
                        return
                    }
                    val index = rankList.indexOfLast {
                        it.userId == AppCacheManager.userId
                    }
                    bindSelfRankInfo(index)
                    rankAdapter.isStateViewEnable = rankList.isEmpty()
                    rankAdapter.submitList(rankList)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun NewYearRankResponse.bindSelfRankInfo(index: Int) {
        mBinding.selfRank.rankInfo = this.myInfo
        mBinding.selfRank.vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.white_corner_15)
        mBinding.selfRank.vgParent.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#Fbebf0"))
        mBinding.selfRank.vgRank.visibility = View.GONE
        if (index >= 0) {
            mBinding.selfRank.tvNum.text = (index+1).toString()
        } else {
            mBinding.selfRank.tvNum.text = "-"
        }
    }

    private var tab1Drawable: Drawable? = null
    private var tab2Drawable: Drawable? = null

    private var imgConfig: NewYearEventImgConfig? = null
    private fun getImageConfig() {
        request(
            { rxApi.getConfigInfo(ServiceConfigKeyManager.KEY_NEW_YEAR_EVENT) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val data1 = data.data
                    imgConfig = GsonUtils.fromJson(data1, NewYearEventImgConfig::class.java)
                    mBinding.eventImageConfig = imgConfig
                    mBinding.executePendingBindings()

                    preloadTab1()
                    preloadTab2()
                }

            })
    }

    private fun preloadTab1(isPreload: Boolean = true) {
        if (imgConfig == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            imgConfig!!.rangTab1,
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
        if (imgConfig == null) {
            return
        }
        GlideUtils.loadAsDrawable(
            mContext,
            imgConfig!!.rangTab2,
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

    override fun initListener() {
        super.initListener()
        initOnScrollChangeListener()
        mBinding.leftTab.setOnSingleClickListener {
            if (!isLeftTab) {
                if (tab1Drawable != null) {
                    mBinding.ivTab.setImageDrawable(tab1Drawable)
                } else {
                    preloadTab1()
                }
                isLeftTab = true
                rankAdapter.isStateViewEnable = dayRankList.isEmpty()
                rankAdapter.submitList(dayRankList)
                getNewYearRank(0)
            }

        }
        mBinding.rightTab.setOnSingleClickListener {
            if (isLeftTab) {
                if (tab2Drawable != null) {
                    mBinding.ivTab.setImageDrawable(tab2Drawable)
                } else {
                    preloadTab2()
                }
                isLeftTab = false
                rankAdapter.isStateViewEnable = totalRankList.isEmpty()
                rankAdapter.submitList(totalRankList)
                getNewYearRank(1)
            }

        }
        mBinding.tvSetCalendar.setOnSingleClickListener {
            checkCalendarPermission()
        }
        mBinding.btnShare.setOnSingleClickListener {
            startWxShare()
        }
    }

    private fun startWxShare() {
        newYearInfo?.apply {
            val shareDTO = this.shareDTO
            val inviteUrl = shareDTO.url
            val contentShare = ContentShare(mContext)
            contentShare.setShareContent(
                shareDTO.title, shareDTO.content, inviteUrl
            )
            contentShare.shareToWeiXin()
        }

    }

    private fun checkCalendarPermission() {
        val permissions = arrayListOf<String>()
        permissions.add(Manifest.permission.READ_CALENDAR)
        permissions.add(Manifest.permission.WRITE_CALENDAR)
        PermissionXUtils.checkPermission(mContext,
            permissions,
            "为了设置抢红包提醒，我们需要申请您的日历权限",
            "您拒绝授权相关权限，无法设置提醒功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    val startTime = CalendarUtils.remindTimeCalculator(2025, 1, 28, 20, 30)
                    val endTime = CalendarUtils.remindTimeCalculator(2025, 1, 28, 20, 40)
                    CalendarUtils.addCalendarEventRemind(mContext,
                        "【暖遇】抢红包提醒",
                        "点击抢红包https://n.huanyuan.cn/YGXJH",
                        startTime,
                        endTime,
                        5,
                        "FREQ=DAILY;COUNT=8",
                        object : CalendarUtils.onCalendarRemindListener {
                            override fun onFailed(error_code: CalendarUtils.onCalendarRemindListener.Status?) {
                                showToast("设置提醒失败")
                            }

                            override fun onSuccess() {
                                showToast("设置提醒成功")
                            }
                        })
                }

                override fun onFail() {
                }

            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getNewYearInfo()
    }


    private var newYearInfo: NewYearInfo? = null
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.newYearInfoLivedata.observe(this) { it ->
            parseState(it, {
                newYearInfo = it
                mBinding.tvTotalReward.text = it.sumRoseNum
                mBinding.tvDivideNum.text = it.devideDesc
                if (it.status !=-2) {
                    startCountTime(it.remainSeconds)
                } else {
                    showFinishView()
                }
            })
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.WX_SHARE_SUCCESS).observe(this) {
            //分享成功
            mViewModel.addVal(mContext, object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    if (isLeftTab) {
                        getNewYearRank(0)
                    } else {
                        getNewYearRank(1)
                    }
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

    @SuppressLint("SetTextI18n")
    private fun showFinishView() {
        mBinding.tvFinishTips.visibility = View.VISIBLE
        mBinding.tvEventTime.visibility = View.INVISIBLE
        mBinding.tvDay.text = "00"
        mBinding.tvHour.text = "00"
        mBinding.tvMinute.text = "00"
    }

    private var countDown: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startCountTime(second: Int) {
        countDown?.cancel()
        countDown(second, start = {
            countDown = it
            setCountDownTime(second)
        }, end = {
            //倒计时结束
            requestData()
        }, next = {
            setCountDownTime(it)
        }, cancel = {})
    }

    private fun setCountDownTime(second: Int) {
        val days = second / (60 * 60 * 24)
        val daysDesc = if (days < 10) {
            "0$days"
        } else {
            days.toString()
        }

        val hours = (second % (60 * 60 * 24)) / (60 * 60)
        val hoursDesc = if (hours < 10) {
            "0$hours"
        } else {
            hours.toString()
        }
        val minutes = (second % (60 * 60)) / 60
        val minutesDesc = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        val secs = second % 60
        val secsDesc = if (secs < 10) {
            "0$secs"
        } else {
            secs.toString()
        }
        val value = if (newYearInfo?.status == 0) {
            "距离下一场开始："
        } else {
            "距离活动开始："
        }
        mBinding.tvTimeDesc.text = value

        if(daysDesc=="00"){
            mBinding.tvDayUnit.text = "时"
            mBinding.tvHourUnit.text = "分"
            mBinding.tvMinuteUnit.text = "秒"
            mBinding.tvDay.text = hoursDesc
            mBinding.tvHour.text = minutesDesc
            mBinding.tvMinute.text = secsDesc
        }else{
            mBinding.tvDayUnit.text = "天"
            mBinding.tvHourUnit.text = "时"
            mBinding.tvMinuteUnit.text = "分"
            mBinding.tvDay.text = daysDesc
            mBinding.tvHour.text = hoursDesc
            mBinding.tvMinute.text = minutesDesc
        }

    }

    override fun exactDestroy() {
        super.exactDestroy()
        showScaleAnim?.cancel()
    }
}
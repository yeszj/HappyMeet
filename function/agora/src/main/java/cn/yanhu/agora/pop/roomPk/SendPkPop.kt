package cn.yanhu.agora.pop.roomPk

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.yanhu.agora.bean.PkTimeInfo
import cn.yanhu.agora.databinding.PopSendRoomPkBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.config.EventBusKeyConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.PkInfoResponse
import cn.yanhu.agora.ui.liveRoom.pk.PkUserListFrg
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.bean.WheelViewPopInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.pop.CommonWheelViewPop
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/9/9
 * desc:
 */
@SuppressLint("ViewConstructor")
class SendPkPop(
    val context: FragmentActivity, val roomId: String, val onPkOperateListener: OnPkOperateListener
) : BottomPopupView(context) {
    private var list: List<PkTimeInfo> = mutableListOf()
    private lateinit var mBinding: PopSendRoomPkBinding
    private var timeList = mutableListOf<String>()
    private var pkSeconds = 5 * 60
    private var targetRoomId: String = ""
    override fun getImplLayoutId(): Int {
        return R.layout.pop_send_room_pk
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSendRoomPkBinding.bind(popupImplView)
        initTabLayout()
        initVpData()
        val build = Spans.builder().text("为你寻找实力相近的对手，预计等待").text("15s")
            .color(CommonUtils.getColor(cn.yanhu.commonres.R.color.colorTextRed)).build()
        mBinding.tvDesc.text = build
        mBinding.viewPager.offscreenPageLimit = 3
        mBinding.tvStartRandom.setOnSingleClickListener {
            sendPkInvite(2)
        }
        mBinding.togglePk.setOnCheckedChangeListener { _, isChecked ->
            request(
                { commonRxApi.switchConfig("can_invite_pk", if (isChecked) 1 else 0) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                    }
                })
        }
        mBinding.tvTime.setOnSingleClickListener {
            showSelectTimePop()
        }
        getTimeInfo()
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_PK_INFO).observe(context) {
            getData()
        }
        mBinding.refreshLayout.setOnRefreshListener {
            if (isShow) {
                LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_PK_INFO).post(false)
                mBinding.refreshLayout.finishRefreshWithNoMoreData()
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun sendPkInvite(source: Int) {
        request(
            { agoraRxApi.initiatePk(roomId, targetRoomId, source, pkSeconds) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    if (source == 2) {
                        dismiss()
                    }
                    onPkOperateListener.onSendInvitePk(source == 2)
                    if (isShow) {
                        fragments.forEach {
                            (it as PkUserListFrg).setInviteSuccess(targetRoomId)
                        }
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    showToast(msg)
                }

            })
    }

    @SuppressLint("CheckResult")
    private fun getTimeInfo() {
        request(
            { commonRxApi.getConfigInfo("pk_time_config") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val data = data.data
                    list = Gson().fromJson(
                        data, object : TypeToken<List<PkTimeInfo>>() {}.type
                    )
                    selectPkTimeInfo = list[1]
                    pkSeconds = selectPkTimeInfo!!.time
                    list.forEach {
                        timeList.add(it.desc)
                    }
                }
            })
    }

    override fun doAfterShow() {
        super.doAfterShow()
        getData()
    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        mBinding.avatarBanner.stopLoop()
    }

    @SuppressLint("CheckResult")
    private fun getData() {
        request(
            { agoraRxApi.getPkRoomInfo(roomId) },
            object : OnRequestResultListener<PkInfoResponse> {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: BaseBean<PkInfoResponse>) {
                    if (isDismiss) {
                        return
                    }
                    val data = response.data ?: return
                    fragments.forEach {
                        (it as PkUserListFrg).setPkInfo(data)
                    }
                    val pkRoomPortraits = data.pkRoomPortraits
                    if (mBinding.avatarBanner.imgUrlList.size != pkRoomPortraits.size) {
                        mBinding.avatarBanner.imgUrlList = pkRoomPortraits
                        mBinding.avatarBanner.initImageView()
                    }
                    CoilImgUtils.loadCircleImg(
                        data.portrait, mBinding.ivMyAvatar, borderWidth = CommonUtils.getDimension(
                            com.zj.dimens.R.dimen.dp_1
                        ).toFloat()
                    )
                    mBinding.togglePk.isChecked = data.invitePk == 1
                    if (data.recommendPkRooms.isNotEmpty()) {
                        mBinding.tvStartRandom.alpha = 1f
                        mBinding.tvStartRandom.isEnabled = true
                        val build =
                            Spans.builder().text("为你寻找实力相近的对手，预计等待").text("15s")
                                .color(CommonUtils.getColor(cn.yanhu.commonres.R.color.colorTextRed))
                                .build()
                        mBinding.tvDesc.text = build
                    } else {
                        mBinding.tvStartRandom.isEnabled = false
                        mBinding.tvStartRandom.alpha = 0.5f
                        mBinding.tvDesc.text = "暂无可PK的对手"
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                }
            })
    }

    private var selectPkTimeInfo: PkTimeInfo? = null

    private fun showSelectTimePop(
    ): CommonWheelViewPop = CommonWheelViewPop.showDialog(
        context,
        timeList,
        selectPkTimeInfo!!.desc,
        object : CommonWheelViewPop.OnSelectWheelListener {
            override fun onSelectValue(value: String) {
                mBinding.tvTime.text = value
                val position = list.indexOfFirst { it.desc == value }
                AppCacheManager.selectTimeIndex = position
                if (position >= 0) {
                    selectPkTimeInfo = list[position]
                    pkSeconds = selectPkTimeInfo!!.time
                }
            }
        },
        WheelViewPopInfo("选择PK时长")
    )


    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(context)
        val list = mutableListOf(
            "推荐列表", "最近PK"
        )
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.viewPager,
            list.toTypedArray(),
            textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_14).toFloat(),
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8),
            selectTextSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_16).toFloat(),
            normalColor = cn.yanhu.baselib.R.color.whiteAlpha20,
            selectColor = cn.yanhu.baselib.R.color.white,
            lineHeight = 0f
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.viewPager)


    }

    val fragments = mutableListOf<Fragment>()

    private fun initVpData() {
        val recommendFrg = PkUserListFrg.newInstance(PkUserListFrg.TYPE_RECOMMEND, roomId)
        val recentFrg = PkUserListFrg.newInstance(PkUserListFrg.TYPE_RECENT, roomId)
        val inviteListener = object : PkUserListFrg.OnInviteListener {
            override fun onInvite(roomId: String) {
                targetRoomId = roomId
                sendPkInvite(1)
            }
        }
        recentFrg.setOnInviteListener(inviteListener)
        recommendFrg.setOnInviteListener(inviteListener)
        fragments.add(recommendFrg)
        fragments.add(recentFrg)

        mBinding.viewPager.offscreenPageLimit = fragments.size
        mBinding.viewPager.adapter = MyFragmentStateAdapter(context, fragments)
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
            }
        })
    }

    interface OnPkOperateListener {
        fun onSendInvitePk(isRandom: Boolean)
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity, roomId: String, onPkOperateListener: OnPkOperateListener
        ): SendPkPop {
            val matchPop = SendPkPop(mContext, roomId, onPkOperateListener)
            val builder = XPopup.Builder(mContext)
            builder.autoOpenSoftInput(false).isViewMode(true).enableDrag(false)
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }
}
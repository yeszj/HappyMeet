package cn.yanhu.agora.ui.liveRoom.create

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.RoomTypeSelectAdapter
import cn.yanhu.agora.bean.LiveTimePriceInfo
import cn.yanhu.agora.bean.RoomConfigInfo
import cn.yanhu.agora.bean.RoomTypeInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.yanhu.agora.databinding.ActivityCreateLiveRoomBinding
import cn.yanhu.agora.pop.BuyLiveTimePop
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.adapter.MyBannerImageAdapter
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager
import com.youth.banner.listener.OnBannerListener

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:创建房间
 */
@Route(path = RouterPath.ROUTER_CREATE_ROOM)
class CreateLiveRoomActivity : BaseActivity<ActivityCreateLiveRoomBinding, LiveRoomViewModel>(
    R.layout.activity_create_live_room,
    LiveRoomViewModel::class.java
) {

    private val roomTypeAdapter by lazy { RoomTypeSelectAdapter() }
    private var createRoomRequest = CreateRoomRequest()
    private lateinit var roomConfigInfo: RoomConfigInfo

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setStatusBarStyle(false)
        mBinding.rvType.adapter = roomTypeAdapter
        mBinding.isFree = true
        roomTypeAdapter.setOnItemClickListener { _, _, position ->
            if (position!=0){
                showToast("该房间类型暂未开放，敬请期待")
                return@setOnItemClickListener
            }
            roomTypeAdapter.setSelectPosition(
                position
            )
            setRestTime()
        }
        val stringExtra = intent.getStringExtra(IntentKeyConfig.DATA)
        if (TextUtils.isEmpty(stringExtra) || stringExtra=="null"){
            finish()
            return
        }
        roomConfigInfo = GsonUtils.fromJson(stringExtra, RoomConfigInfo::class.java)
        createRoomRequest.name = roomConfigInfo.name
        createRoomRequest.welcomeMsg = roomConfigInfo.welcomeMsg
        rechargeAgreement = roomConfigInfo.liveAgreement
        mBinding.roomInfo = createRoomRequest
        roomTypeAdapter.submitList(roomConfigInfo.types)
        bindBanner(roomConfigInfo.banners)
        setAgreementInfo()
        setRestTime()
    }

    private fun setAgreementInfo() {
        val build = Spans.builder().text("同意并接受")
            .text("《直播协议》")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain)).click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            RouteIntent.lunchToWebView(WebUrlManager.LIVE_ROOM_AGREEMENT)
                        }
                    })
            ).build()
        mBinding.tvAgreement.text = build
    }


    private fun bindBanner(list: MutableList<BannerBean>) {
        if (list.size <= 0) {
            mBinding.banner.visibility = View.GONE
        }
        mBinding.banner.addBannerLifecycleObserver(this)
        mBinding.banner.setAdapter(MyBannerImageAdapter(mBinding.banner, list))
        mBinding.banner.setOnBannerListener(object : OnBannerListener<BannerBean> {
            override fun OnBannerClick(data: BannerBean, position: Int) {
                PageIntentUtil.url2Page(ActivityUtils.getTopActivity(), data.pageUrl)
            }
        })
    }


    private var selectTypeItem: RoomTypeInfo? = null

    @SuppressLint("SetTextI18n")
    private fun setRestTime() {
        selectTypeItem = roomTypeAdapter.getSelectItem()
        selectTypeItem?.apply {
            createRoomRequest.roomType = this.type
//            mBinding.tvTimeType.text = "${this.name}开播时长"
//            mBinding.tvRestTime.text = "剩余 ${this.restDay} 天"
//            mBinding.isFree = this.isFree
        }
    }

    private var isCheck = false
    override fun initListener() {
        super.initListener()
        mBinding.bgFace.setOnSingleClickListener { RouteIntent.lunchToBeautifulFace() }
        mBinding.bgCreate.setOnSingleClickListener { createRoom() }
        mBinding.tvBuyTime.setOnSingleClickListener { showBuyLiveTimePop() }
        mBinding.ivAgreement.setOnSingleClickListener {
            if (isCheck) {
                isCheck = false
                mBinding.ivAgreement.setImageResource(cn.yanhu.commonres.R.drawable.svg_unselected_r20)
            } else {
                isCheck = true
                mBinding.ivAgreement.setImageResource(cn.yanhu.commonres.R.drawable.svg_selected_r20)
            }
        }
    }


    private fun createRoom() {
//        selectTypeItem?.apply {
//            if (!this.isFree && !CommonUtils.compareZero(this.restDay)) {
//                showToast("请购买开播时长")
//                showBuyLiveTimePop()
//                return
//            }
//        }
//        if (TextUtils.isEmpty(createRoomRequest.name)) {
//            showToast("请输入房间名称")
//            return
//        }
        if (!isCheck) {
            showToast("请阅读并同意直播协议")
            return
        }
        if (TextUtils.isEmpty(createRoomRequest.welcomeMsg)) {
            showToast("请输入房间欢迎语")
            return
        }
        mViewModel.createRoom(createRoomRequest)
    }

    private fun showBuyLiveTimePop() {
        BuyLiveTimePop.showDialog(mContext, timePriceList, rechargeAgreement)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onCreateRoomResult()
        onPayResult()
    }

    private fun onPayResult() {
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                requestData()
            }
        })
    }

    private fun onCreateRoomResult() {
        mViewModel.createRoomLivedata.observe(this) {
            parseState(it, { roomInfo ->
                //创建成功 进入房间页面
                showToast("创建成功")
                RouteIntent.lunchToLiveRoom(mContext, roomInfo)
                LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.CLOSELIVEROOM,roomInfo.roomId.toString())
                finish()
            })
        }
    }

    private var timePriceList: MutableList<LiveTimePriceInfo> = mutableListOf()
    private var rechargeAgreement: String = ""
}
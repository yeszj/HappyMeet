package cn.yanhu.agora.ui.liveRoom.create

import android.annotation.SuppressLint
import android.text.TextUtils
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.RoomTypeSelectAdapter
import cn.yanhu.agora.bean.LiveTimePriceInfo
import cn.yanhu.agora.bean.RoomTypeInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.yanhu.agora.databinding.ActivityCreateLiveRoomBinding
import cn.yanhu.agora.pop.BuyLiveTimePop
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager

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

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setStatusBarStyle(false)
        mBinding.rvType.adapter = roomTypeAdapter
        roomTypeAdapter.setOnItemClickListener { _, _, position ->
            roomTypeAdapter.setSelectPosition(
                position
            )
            setRestTime()
        }
        requestData()
    }

    private var selectTypeItem: RoomTypeInfo? = null

    @SuppressLint("SetTextI18n")
    private fun setRestTime() {
        selectTypeItem = roomTypeAdapter.getSelectItem()
        selectTypeItem?.apply {
            createRoomRequest.roomType = this.type
            mBinding.tvTimeType.text = "${this.name}开播时长"
            mBinding.tvRestTime.text = "剩余 ${this.restDay} 天"
            mBinding.isFree = this.isFree
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.bgFace.setOnSingleClickListener { RouteIntent.lunchToBeautifulFace() }
        mBinding.bgCreate.setOnSingleClickListener { createRoom() }
        mBinding.tvBuyTime.setOnSingleClickListener { showBuyLiveTimePop() }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRoomConfigInfo()
    }

    private fun createRoom() {
        selectTypeItem?.apply {
            if (!this.isFree && !CommonUtils.compareZero(this.restDay)) {
                showToast("请购买开播时长")
                showBuyLiveTimePop()
                return
            }
        }
        if (TextUtils.isEmpty(createRoomRequest.name)) {
            showToast("请输入房间名称")
            return
        }
        if (TextUtils.isEmpty(createRoomRequest.welcomeMsg)) {
            showToast("请输入房间欢迎语")
            return
        }
        mViewModel.createRoom(createRoomRequest)
    }

    private fun showBuyLiveTimePop() {
        BuyLiveTimePop.showDialog(mContext, timePriceList,rechargeAgreement)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onGetConfigResult()
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
            parseState(it, { roomId ->
                //创建成功 进入房间页面
                showToast("创建成功")
                RouteIntent.lunchToLiveRoom(createRoomRequest.roomType, roomId)
                finish()
            })
        }
    }

    private var timePriceList: MutableList<LiveTimePriceInfo> = mutableListOf()
    private var rechargeAgreement:String = ""
    private fun onGetConfigResult() {
        mViewModel.roomConfigInfoLivedata.observe(this) { it ->
            parseState(it, {
                createRoomRequest.name = it.name
                createRoomRequest.welcomeMsg = it.welcomeMsg
                rechargeAgreement = it.rechargeAgreement
                timePriceList = it.timePriceList
                Glide.with(mContext).load(it.bannerUrl)
                    .into(mBinding.ivBanner)
                mBinding.roomInfo = createRoomRequest
                roomTypeAdapter.submitList(it.typeList)
                setRestTime()
            })
        }
    }
}
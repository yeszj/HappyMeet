package cn.yanhu.agora.ui.liveRoom.live

import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.yanhu.agora.R
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.agora.databinding.ActivitySlideLiveRoomBinding
import cn.yanhu.agora.listener.SwitchRoomListener
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.smart.adapter.SmartViewPager2Adapter
import com.smart.adapter.interf.OnLoadMoreListener

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
 class SlideLiveRoomActivity : BaseActivity<ActivitySlideLiveRoomBinding, LiveRoomViewModel>(
    R.layout.activity_slide_live_room,
    LiveRoomViewModel::class.java
) {

    private var currentPage: Int = 1
    private val mAdapter by lazy {
        SmartViewPager2Adapter.Builder<RoomDetailInfo>(this)
            .overScrollNever()
            .canScroll(false)
            .setOffscreenPageLimit(1)
            .setPreLoadLimit(3)
            .addFragment(RoomListBean.FRG_THREE_ROOM, ThreeLiveRoomFrg::class.java)
            .addFragment(RoomListBean.FRG_SEVEN_ROOM, SevenLiveRoomFrg::class.java)
            .addFragment(RoomListBean.FRG_NINE_ROOM, NineLiveRoomFrg::class.java)
            .addDefaultFragment(NeedUpgradeTipFrg::class.java)
            .build(mBinding.viewPager)
    }

    override fun initData() {
        setFullScreenStatusBar(true)
        initRoomVpAdapter()
        sdkInit()
        AgoraManager.isLiveRoom = true
    }

    private fun sdkInit(){
        //声网初始化
        AgoraManager.getInstence().init(mContext, 0, null)
        AgoraManager.getInstence().setVideoEncoderConfiguration(250, 280)
    }


    override fun initListener() {
        super.initListener()
        initLoadMoreListener()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRoomList(currentPage)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.roomListObservable.observe(this) { it ->
            parseState(it, {
                mAdapter.addData(getRoomList(it.roomList))
            })
        }
    }

    private fun initLoadMoreListener() {
        mAdapter.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore(smartAdapter: SmartViewPager2Adapter<*>) {
                currentPage++
                requestData()
            }
        })
    }

    private fun initRoomVpAdapter() {
        val data = intent.getStringExtra(IntentKeyConfig.DATA)
        currentPage = intent.getIntExtra(IntentKeyConfig.PAGE, -1)
        val list = GsonUtils.fromJson<MutableList<RoomDetailInfo>>(
            data,
            object : TypeToken<List<RoomDetailInfo>>() {}.type
        )
        currentRoomId = intent.getStringExtra(IntentKeyConfig.ROOM_ID)
        val roomList = getRoomList(list)
        val currentPosition = roomList.indexOfFirst { it.id.toString() == currentRoomId }
        mAdapter.addData(roomList)
        mBinding.viewPager.adapter = mAdapter
        if (currentPosition != -1) {
            mBinding.viewPager.setCurrentItem(currentPosition, false)
        }
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val item = mAdapter.getItem(position) as RoomListBean
                switchViewPager(item.id.toString())
            }
        })
    }

    private fun getRoomList(list: MutableList<RoomDetailInfo>): MutableList<RoomDetailInfo> {
        val roomList: MutableList<RoomDetailInfo> = mutableListOf()
        list.forEach {
            if (it.roomType != 0) {
                roomList.add(it)
            }
        }
        return roomList
    }

    private val switchRoomListenerMap: HashMap<String, SwitchRoomListener> =  HashMap()
    private var currentRoomId: String? = null
     fun switchViewPager(roomId: String) {
        if (roomId!=currentRoomId) {
            if (currentRoomId != null && switchRoomListenerMap.containsKey(currentRoomId)) {
                switchRoomListenerMap[currentRoomId]?.destroyRoom()
            }
            currentRoomId = roomId
            if (switchRoomListenerMap.containsKey(currentRoomId)) {
                switchRoomListenerMap[currentRoomId]?.preJoinRoom()
            }
        }
    }

     fun addSwitchRoomListener(roomId: String, switchRoomListener: SwitchRoomListener) {
        switchRoomListenerMap[roomId] = switchRoomListener
    }

     fun removeSwitchRoomListener(roomId: String) {
        switchRoomListenerMap.remove(roomId)
    }

    override fun exactDestroy() {
        super.exactDestroy()
        AgoraManager.isLiveRoom = false
    }

    companion object{
        const val LIVE_ROOM_TAG = "liveRoom"
    }
}
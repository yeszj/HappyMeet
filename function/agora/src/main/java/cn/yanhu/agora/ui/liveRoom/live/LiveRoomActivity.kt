package cn.yanhu.agora.ui.liveRoom.live

import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityLiveRoomBinding
import cn.yanhu.agora.listener.SwitchRoomListener
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.smart.adapter.SmartViewPager2Adapter
import com.smart.adapter.interf.OnLoadMoreListener

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
@Route(path = RouterPath.ROUTER_LIVE_ROOM)
 class LiveRoomActivity : BaseActivity<ActivityLiveRoomBinding, LiveRoomViewModel>(
    R.layout.activity_live_room,
    LiveRoomViewModel::class.java
) {

    private var currentPage: Int = 1
    private val mAdapter by lazy {
        SmartViewPager2Adapter(this, mBinding.viewPager)
            .cancleOverScrollMode()
            .setOffscreenPageLimit(1)
            .setPreLoadLimit(3)
            .addFragment(RoomListBean.TYPE_THREE_ROOM, ThreeLiveRoomFrg::class.java)
            .addFragment(RoomListBean.TYPE_SEVEN_ROOM, SevenLiveRoomFrg::class.java)
            .addFragment(RoomListBean.TYPE_OTHER_ROOM, NeedUpgradeTipFrg::class.java)
    }

    override fun initData() {
        setFullScreenStatusBar(true)
        initRoomVpAdapter()

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
        mAdapter.setLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore(smartAdapter: SmartViewPager2Adapter) {
                currentPage++
                requestData()
            }
        })
    }

    private fun initRoomVpAdapter() {
        val data = intent.getStringExtra(IntentKeyConfig.DATA)
        currentPage = intent.getIntExtra(IntentKeyConfig.PAGE, -1)
        val list = GsonUtils.fromJson<MutableList<RoomListBean>>(
            data,
            object : TypeToken<List<RoomListBean>>() {}.type
        )
        currentRoomId = intent.getStringExtra(IntentKeyConfig.ROOM_ID)
        val roomList = getRoomList(list)
        val currentPosition = roomList.indexOfFirst { it.roomId == currentRoomId }
        mAdapter.addData(roomList)
        mBinding.viewPager.adapter = mAdapter
        if (currentPosition != -1) {
            mBinding.viewPager.setCurrentItem(currentPosition, false)
        }
        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val item = mAdapter.getItem(position) as RoomListBean
                switchViewPager(item.roomId!!)
            }
        })
    }

    private fun getRoomList(list: MutableList<RoomListBean>): MutableList<RoomListBean> {
        val roomList: MutableList<RoomListBean> = mutableListOf()
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

    companion object{
        const val LIVE_ROOM_TAG = "liveRoom"
    }
}
package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.RoomPkUserAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.PkConfigInfo
import cn.yanhu.agora.bean.PkSeatUserInfo
import cn.yanhu.agora.databinding.PopRoomPkSendBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomListBean.Companion.TYPE_SEVEN_SONG
import cn.yanhu.commonres.bean.WheelViewPopInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.pop.CommonWheelViewPop
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import kotlin.math.max

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
@SuppressLint("ViewConstructor")
class RoomPkSendPop(
    context: Context,
    val pkConfigInfo: PkConfigInfo?,
    val roomInfo: RoomListBean,
    val onSelectUserListener: OnSelectUserListener
) :
    BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            pkConfigInfo: PkConfigInfo?,
            roomInfo: RoomListBean,
            onSelectUserListener: OnSelectUserListener,
            simpleCallback: SimpleCallback
        ): RoomPkSendPop {
            val matchPop = RoomPkSendPop(mContext, pkConfigInfo, roomInfo, onSelectUserListener)
            val builder = XPopup.Builder(mContext)
                .setPopupCallback(simpleCallback)
                .enableDrag(false)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_room_pk_send
    }

    private lateinit var mBinding: PopRoomPkSendBinding
    private val redUserAdapter by lazy { RoomPkUserAdapter() }
    private val blueUserAdapter by lazy { RoomPkUserAdapter() }
    private var selectPkTimeInfo: PkConfigInfo.PlTimeInfo? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRoomPkSendBinding.bind(popupImplView)
        mBinding.rvBlue.adapter = blueUserAdapter
        mBinding.rvRed.adapter = redUserAdapter
        val initList = mutableListOf<PkSeatUserInfo>(PkSeatUserInfo(0, 0))
        blueUserAdapter.submitList(initList)
        redUserAdapter.submitList(initList)
        redUserAdapter.setOnItemClickListener { _, _, position ->
            val item = redUserAdapter.getItem(position)
            if (item ==null || TextUtils.isEmpty(item.portrait)) {
                showSelectUser(true)
            }
        }
        redUserAdapter.addOnItemChildClickListener(R.id.iv_delete) { _, _, position ->
            val item = redUserAdapter.getItem(position) ?: return@addOnItemChildClickListener
            setUserLeave(item.userId, true)
        }
        blueUserAdapter.setOnItemClickListener { _, _, position ->
            val item = blueUserAdapter.getItem(position)
            if (item==null || TextUtils.isEmpty(item.portrait)) {
                showSelectUser(false)
            }
        }
        blueUserAdapter.addOnItemChildClickListener(R.id.iv_delete) { _, _, position ->
            val item = blueUserAdapter.getItem(position) ?: return@addOnItemChildClickListener
            setUserLeave(item.userId, true)
        }
        mBinding.tvStartPk.setOnSingleClickListener {
            startPk()
        }
        initPkModeChange()
        initTimeConfig()
    }

    private fun startPk() {
        val redIdList = mutableListOf<String>()
        selectRedList.forEach {
            if (!TextUtils.isEmpty(it.userId)) {
                redIdList.add(it.userId)
            }
        }

        val blueIdList = mutableListOf<String>()
        selectBlueList.forEach {
            if (!TextUtils.isEmpty(it.userId)) {
                blueIdList.add(it.userId)
            }
        }
        request({
            agoraRxApi.startPk(
                roomInfo.roomId,
                GsonUtils.toJson(redIdList),
                GsonUtils.toJson(blueIdList),
                if (isSingMode) 1 else 0,
                selectPkTimeInfo!!.choiceType.toString()
            )
        }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                dismiss()
            }
        })
    }

    private fun showSelectUser(isSelectRedUser: Boolean) {
        onSelectUserListener.onSelectUser(isSelectRedUser, isSingMode)
    }

    var selectRedList = mutableListOf<PkSeatUserInfo>()
    var selectBlueList = mutableListOf<PkSeatUserInfo>()
    private var isSingMode = false
    private fun initPkModeChange() {
        mBinding.tvStartPk.isEnabled = false
        val defaultSelect = pkConfigInfo?.pkTypeDefaultIndex ?: 0
        isSingMode = defaultSelect == 0
        mBinding.radioGroup.check(mBinding.radioGroup.getChildAt(defaultSelect).id)
        mBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_single) {
                isSingMode = true
                if (selectRedList.size > 1) {
                    val list = mutableListOf<PkSeatUserInfo>(selectRedList[0])
                    redUserAdapter.submitList(list)
                    for (i in selectRedList.size-1 downTo 1) {
                        if (!TextUtils.isEmpty(selectRedList[i].userId)){
                            onSelectUserListener.onDeleteUser(
                                selectRedList[i].userId, true
                            )
                        }

                    }
                    setSelectCount(true)
                }
                if (selectBlueList.size > 1) {
                    val list = mutableListOf<PkSeatUserInfo>(selectBlueList[0])
                    blueUserAdapter.submitList(list)
                    for (i in selectBlueList.size-1 downTo 1) {
                        if (!TextUtils.isEmpty(selectBlueList[i].userId)){
                            onSelectUserListener.onDeleteUser(
                                selectBlueList[i].userId, true
                            )
                        }
                    }
                    setSelectCount(false)
                }
            } else {
                isSingMode = false
                val maxCount =
                    if (roomInfo.getFragmentType() == RoomListBean.FRG_SEVEN_ROOM || roomInfo.roomType == TYPE_SEVEN_SONG) {
                        6
                    } else {
                        8
                    }
                if (selectRedList.isNotEmpty()) {
                    redUserAdapter.submitList(selectRedList)
                    if (redUserAdapter.items.size < maxCount && !redUserAdapter.hasAddItem()) {
                        redUserAdapter.add(PkSeatUserInfo(0, 0))
                    }
                    onSelectUserListener.onSelectUserResult(selectRedList,true)
                    setSelectCount(true)
                }
                if (selectBlueList.isNotEmpty()) {
                    blueUserAdapter.submitList(selectBlueList)
                    if (blueUserAdapter.items.size < maxCount && !blueUserAdapter.hasAddItem()) {
                        blueUserAdapter.add(PkSeatUserInfo(0, 0))
                    }
                    onSelectUserListener.onSelectUserResult(selectBlueList,false)
                    setSelectCount(false)
                }
            }
        }
    }

    private fun initTimeConfig() {
        pkConfigInfo?.apply {
            AppCacheManager.selectTimeIndex = pkConfigInfo.pkTimeDefaultIndex
            selectPkTimeInfo = pkConfigInfo.pkTimeList[pkConfigInfo.pkTimeDefaultIndex]
            mBinding.tvTime.text = selectPkTimeInfo!!.showText
            val timeValueList = mutableListOf<String>()
            pkConfigInfo.pkTimeList.forEach {
                timeValueList.add(it.showText)
            }
            mBinding.tvTime.setOnSingleClickListener {
                showSelectTimePop(timeValueList)
            }
        }
    }

    private fun showSelectTimePop(
        timeValueList: MutableList<String>
    ): CommonWheelViewPop = CommonWheelViewPop.showDialog(
        context,
        timeValueList,
        selectPkTimeInfo!!.showText,
        object : CommonWheelViewPop.OnSelectWheelListener {
            override fun onSelectValue(value: String) {
                mBinding.tvTime.text = value
                val position = pkConfigInfo!!.pkTimeList.indexOfFirst { it.showText == value }
                AppCacheManager.selectTimeIndex = position
                if (position >= 0) {
                    selectPkTimeInfo = pkConfigInfo.pkTimeList[position]
                }
            }
        },
        WheelViewPopInfo("选择PK时长")
    )

    @SuppressLint("SetTextI18n")
    fun setSelectUser(infos: MutableList<PkSeatUserInfo>, isSelectRedUser: Boolean, maxCount: Int) {
        if (isSelectRedUser) {
            if (isRedTips && mBinding.tvTips.isVisible) {
                mBinding.tvTips.visibility = GONE
            }
        } else {
            if (!isRedTips && mBinding.tvTips.isVisible) {
                mBinding.tvTips.visibility = GONE
            }
        }
        if (isSelectRedUser) {
            if (infos.size != maxCount) {
                infos.add(PkSeatUserInfo(0, 0))
            }
            infos.forEach { selectItem ->
                selectBlueList.removeIf { it.userId == selectItem.userId }
            }
            redUserAdapter.submitList(infos)
            selectRedList = redUserAdapter.items.toMutableList()
        } else {
            infos.forEach { selectItem ->
                selectRedList.removeIf { it.userId == selectItem.userId }
            }
            if (infos.size != maxCount) {
                infos.add(PkSeatUserInfo(0, 0))
            }
            blueUserAdapter.submitList(infos)
            selectBlueList = blueUserAdapter.items.toMutableList()
        }
        setSelectCount(isSelectRedUser)

        changeBtnStatus()
    }

    @SuppressLint("SetTextI18n")
    private fun setSelectCount(isRed: Boolean) {
        if (isRed) {
            val redIdList = mutableListOf<String>()
            redUserAdapter.items.forEach {
                if (!TextUtils.isEmpty(it.userId)) {
                    redIdList.add(it.userId)
                }
            }
            mBinding.tvRedCount.text = "${redIdList.size}人"
        } else {
            val blueIdList = mutableListOf<String>()
            blueUserAdapter.items.forEach {
                if (!TextUtils.isEmpty(it.userId)) {
                    blueIdList.add(it.userId)
                }
            }
            mBinding.tvBlueCount.text = "${blueIdList.size}人"
        }


    }

    private fun changeBtnStatus() {
        mBinding.tvStartPk.isEnabled = hasRedUser() && hasBlueUser()
    }

    private fun hasRedUser(): Boolean {
        redUserAdapter.items.forEach {
            if (!TextUtils.isEmpty(it.userId)) {
                return true
            }
        }
        return false
    }

    private fun hasBlueUser(): Boolean {
        blueUserAdapter.items.forEach {
            if (!TextUtils.isEmpty(it.userId)) {
                return true
            }
        }
        return false
    }


    private var isRedTips = false
    fun setUserLeave(leaveUserId: String, isDelete: Boolean = false) {
        val maxCount = if (isSingMode) {
            1
        } else {
            if (roomInfo.getFragmentType() == RoomListBean.FRG_SEVEN_ROOM || roomInfo.roomType == TYPE_SEVEN_SONG) {
                6
            } else {
                8
            }
        }
        for (i in redUserAdapter.items.indices) {
            val item = redUserAdapter.getItem(i)
            if (item?.userId == leaveUserId.toString()) {
                redUserAdapter.removeAt(i)
                onSelectUserListener.onDeleteUser(leaveUserId, true)
                if (redUserAdapter.items.size < maxCount && !redUserAdapter.hasAddItem()) {
                    redUserAdapter.add(PkSeatUserInfo(0, 0))
                }
                selectRedList = redUserAdapter.items.toMutableList()
                if (!isDelete) {
                    isRedTips = true
                    mBinding.tvTips.visibility = VISIBLE
                    if (redUserAdapter.itemCount == 1 && TextUtils.isEmpty(redUserAdapter.getItem(0)?.portrait)) {
                        mBinding.tvTips.text = "红方下麦了，请重新添加红方成员"
                    } else {
                        mBinding.tvTips.text = "红方有人下麦了，成员列表已更新"
                    }
                }
                setSelectCount(true)
            }
        }
        for (i in blueUserAdapter.items.indices) {
            val item = blueUserAdapter.getItem(i)
            if (item?.userId == leaveUserId.toString()) {
                blueUserAdapter.removeAt(i)
                onSelectUserListener.onDeleteUser(leaveUserId, false)
                if (blueUserAdapter.items.size < maxCount && !blueUserAdapter.hasAddItem()) {
                    blueUserAdapter.add(PkSeatUserInfo(0, 0))
                }
                selectBlueList = blueUserAdapter.items.toMutableList()
                if (!isDelete) {
                    isRedTips = false
                    mBinding.tvTips.visibility = VISIBLE
                    if (blueUserAdapter.itemCount == 1 && TextUtils.isEmpty(
                            blueUserAdapter.getItem(
                                0
                            )?.portrait
                        )
                    ) {
                        mBinding.tvTips.text = "蓝方下麦了，请重新添加蓝方成员"
                    } else {
                        mBinding.tvTips.text = "蓝方有人下麦了，成员列表已更新"
                    }
                }
                setSelectCount(false)
            }
        }
        changeBtnStatus()
    }

    interface OnSelectUserListener {
        fun onSelectUser(isSelectRedUser: Boolean, isSingMode: Boolean)
        fun onDeleteUser(userId: String, isSelectRedUser: Boolean)
        fun onSelectUserResult(userList: MutableList<PkSeatUserInfo>,isSelectRedUser: Boolean)
    }
}
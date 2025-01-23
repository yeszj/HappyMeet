package cn.yanhu.agora.pop.song

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.song.SongUserChooseAdapter
import cn.yanhu.agora.databinding.PopChooseSongBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SeatUserInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
@SuppressLint("ViewConstructor")
class ChooseSongPop(
    context: Context,
    val list: MutableList<RoomSeatInfo>,
    private val ownerUserId: String,
    private val selectUserId:String,
    val giftInfo: GiftInfo?,
    private val onClickSongListener: OnClickSongListener
) : BottomPopupView(context) {
    private val userAdapter by lazy { SongUserChooseAdapter(ownerUserId) }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            list: MutableList<RoomSeatInfo>,
            ownerUserId: String,
            selectUserId:String,
            giftInfo: GiftInfo?,
            onClickSongListener: OnClickSongListener
        ): ChooseSongPop {
            val matchPop = ChooseSongPop(mContext, list, ownerUserId,selectUserId,giftInfo,onClickSongListener)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_choose_song
    }

    private lateinit var mBinding: PopChooseSongBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopChooseSongBinding.bind(popupImplView)
        mBinding.giftInfo = giftInfo
        mBinding.vgGift.visibility = View.VISIBLE
        val userList = mutableListOf<SeatUserInfo>()
        list.forEach {
            val roomUserSeatInfo = it.roomUserSeatInfo
            if (roomUserSeatInfo != null && (roomUserSeatInfo.userId  == ownerUserId || roomUserSeatInfo.userId!=AppCacheManager.userId)) {
                roomUserSeatInfo.seatId = it.id
                userList.add(roomUserSeatInfo)
            }
        }
        val index = userList.indexOfFirst { it.userId == selectUserId }
        mBinding.rvUser.adapter = userAdapter
        userAdapter.submitList(userList)
        if (index>=0){
            userAdapter.setSelectPosition(index)
        }
        userAdapter.setOnItemClickListener { _, _, position ->
            userAdapter.setSelectPosition(
                position
            )
        }
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
        mBinding.tvClick.setOnSingleClickListener {
            val selectItem = userAdapter.getSelectItem()
            if (selectItem==null){
                showToast("请选择用户")
            }else{
                onClickSongListener.onClickSong(selectItem)
            }
        }
        mBinding.tvDesc.setOnSingleClickListener {
            onClickSongListener.onShowSongList()
        }
    }

    interface OnClickSongListener{
        fun onClickSong(seatUserInfo:SeatUserInfo)
        fun onShowSongList()
    }
}
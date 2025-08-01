package cn.yanhu.agora.pop.song

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.song.SongListAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.SongInfo
import cn.yanhu.agora.bean.SongListResponse
import cn.yanhu.agora.databinding.PopSongListBinding
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ScreenUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
@SuppressLint("ViewConstructor")
class SongListPop(
    context: Context,
    private var songInfo: SongListResponse,
    val isOwner: Boolean,
    val onClickFinishListener: OnRefreshSeatListener
) : BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            songInfo: SongListResponse,
            isOwner: Boolean,
            onClickFinishListener: OnRefreshSeatListener
        ): SongListPop {
            val matchPop = SongListPop(mContext, songInfo, isOwner, onClickFinishListener)
            val builder =
                XPopup.Builder(mContext).maxHeight((ScreenUtils.getScreenHeight() * 0.7).toInt())
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    private val songListAdapter by lazy { SongListAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_song_list
    }

    private lateinit var mBinding: PopSongListBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSongListBinding.bind(popupImplView)
        mBinding.rvSong.adapter = songListAdapter
        if (isOwner) {
            mBinding.tvFinish.visibility = VISIBLE
        } else {
            mBinding.tvFinish.visibility = GONE
        }
        bindData()
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
        mBinding.tvFinish.setOnSingleClickListener {
            showFinishTip()
        }
        songListAdapter.addOnItemChildClickListener(R.id.iv_start,
            object : BaseQuickAdapter.OnItemChildClickListener<SongInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<SongInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = songListAdapter.getItem(position) ?: return
                    startSong(item)
                }

            })
    }

    @SuppressLint("SetTextI18n")
    private fun bindData() {
        if (songInfo.songInfo != null && !TextUtils.isEmpty(songInfo.songInfo!!.userId)) {
            mBinding.songUser.vgSong.visibility = VISIBLE
            mBinding.songUser.songInfo = songInfo.songInfo
            mBinding.tvTips.visibility = GONE
        } else {
            mBinding.songUser.vgSong.visibility = GONE
            mBinding.tvTips.visibility = VISIBLE
        }
        songListAdapter.setIsShowStartBtn(isOwner && songInfo.songInfo==null)
        val list = songInfo.list
        if (list.isEmpty()) {
            mBinding.tvEmpty.visibility = VISIBLE
        } else {
            mBinding.tvEmpty.visibility = GONE
        }
        mBinding.tvNum.text = "等待演唱（${list.size}）"
        songListAdapter.submitList(list)
    }

    fun refreshData(response: SongListResponse?) {
        this.songInfo = response ?: return
        bindData()
    }

    private fun showFinishTip() {
        DialogUtils.showConfirmDialog("结束本轮演唱", {
            clearSongRose()
        }, {
        }, "结束演唱将重置所有嘉宾的插队玫瑰数，确定要结束吗？", confirm = "确定结束")
    }

    private fun clearSongRose() {
        request2(
            { agoraRxApi.clearSongRose(songInfo.roomId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("插队玫瑰数已重置")
                    onClickFinishListener.onClearSongRoseSuccess()
                    dismiss()
                }
            })
    }

    private fun startSong(item: SongInfo) {
        request2(
            { agoraRxApi.setSongUser(item.id)},
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    onClickFinishListener.onSetSongUserSuccess()
                }
            })


    }

    interface OnRefreshSeatListener {
        fun onClearSongRoseSuccess()
        fun onSetSongUserSuccess()
    }
}
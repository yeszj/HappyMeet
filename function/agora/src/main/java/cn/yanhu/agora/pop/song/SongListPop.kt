package cn.yanhu.agora.pop.song

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.song.SongListAdapter
import cn.yanhu.agora.bean.SongListResponse
import cn.yanhu.agora.databinding.PopSongListBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
@SuppressLint("ViewConstructor")
class SongListPop(context: Context, private val songInfo:SongListResponse) : BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            songInfo: SongListResponse,
        ): SongListPop {
            val matchPop = SongListPop(mContext, songInfo)
            val builder =
                XPopup.Builder(mContext).maxHeight((ScreenUtils.getScreenHeight()*0.7).toInt())
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    private val songListAdapter by lazy { SongListAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_song_list
    }

    private lateinit var mBinding:PopSongListBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSongListBinding.bind(popupImplView)
        mBinding.rvSong.adapter = songListAdapter
        val list = songInfo.list
        if (list.size<=0){
            mBinding.tvEmpty.visibility = View.VISIBLE
        }else{
            mBinding.tvEmpty.visibility = View.GONE
        }
        mBinding.tvNum.text = "等待演唱（${list.size}）"
        songListAdapter.submitList(list)
        mBinding.ivClose.setOnSingleClickListener { dismiss() }
    }
}
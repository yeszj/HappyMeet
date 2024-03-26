package cn.yanhu.dynamic.ui.frg

import android.graphics.Color
import android.view.View
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.VideoCacheProxy
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.VideoUtils
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.adapter.DynamicAdapter
import cn.yanhu.dynamic.databinding.FrgSameCityDynamicListBinding
import cn.yanhu.dynamic.ui.DynamicViewModel
import cn.yanhu.dynamic.ui.activity.DynamicDetailActivity
import cn.zj.netrequest.ext.parseState
import com.bumptech.glide.Glide
import com.jeremyliao.liveeventbus.LiveEventBus
import xyz.doikki.videoplayer.player.BaseVideoView
import xyz.doikki.videoplayer.player.VideoView

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class DynamicListFrg : BaseFragment<FrgSameCityDynamicListBinding, DynamicViewModel>(
    R.layout.frg_same_city_dynamic_list,
    DynamicViewModel::class.java
) {
    private val dynamicAdapter by lazy { DynamicAdapter() }
    private var page = 1
    override fun initData() {
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        dynamicAdapter.setHasStableIds(true)
        mBinding.recyclerView.adapter = dynamicAdapter
        requestData()
        initVideoView()

    }

    override fun lazyLoad() {

    }

    override fun initListener() {
        addOnChildAttachStateChangeListener()
        addRvScrollChangeListener()
        mBinding.ivPub.setOnSingleClickListener {
            RouteIntent.lunchPubDynamic()
        }
        dynamicAdapter.setOnItemClickListener { _, _, position ->
            val item = dynamicAdapter.getItem(position)
            item?.apply {
                DynamicDetailActivity.lunch(mContext, id.toString())
            }
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getDynamicList(page)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onLikeMoment()
        onUnlikeMoment()
        onGetDynamicSuccess()
        onDeleteCallBack()
        onDeleteDiscussSuccess()
    }

    /**
     * 详情中 删除评论成功
     */
    private fun onDeleteCallBack() {
        LiveEventBus.get<String>(LiveDataEventManager.DELETE_MOMENT_SUCCESS).observe(this) {
            for (i in dynamicAdapter.items.indices) {
                val item = dynamicAdapter.getItem(i)
                if (item!!.id.toString() == it) {
                    dynamicAdapter.removeAt(i)
                    return@observe
                }
            }
        }
    }

    private fun onGetDynamicSuccess() {
        mViewModel.dynamicListObserver.observe(this) { it ->
            parseState(it, {
                if (page == 1) {
                    dynamicAdapter.submitList(it)
                } else {
                    dynamicAdapter.addAll(it)
                }
                setDataLoadFinish(page, it.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }



    private fun onUnlikeMoment() {
        LiveEventBus.get<String>(LiveDataEventManager.UNLIKE_MOMENT_SUCCESS).observe(this) {
            for (i in dynamicAdapter.items.indices) {
                val item = dynamicAdapter.getItem(i)
                item?.apply {
                    if (id.toString() == it) {
                        //取消点赞动态
                        trendsIsLike = false
                        trendsLikeCount--
                        dynamicAdapter.notifyItemChanged(i, DynamicAdapter.UPDATE_LIKE)
                        return@observe
                    }
                }

            }
        }
    }

    /**
     * 删除评论成功 更新列表评论数量
     */
    private fun onDeleteDiscussSuccess() {
        LiveEventBus.get<String>(LiveDataEventManager.DELETE_DISCUSS_SUCCESS).observe(this) {
            if (it != null) {
                val data = dynamicAdapter.items
                for (position in data.indices) {
                    val momentInfo = data[position]
                    if (momentInfo.id.toString() == it) {
                        momentInfo.trendsCommentCount--
                        dynamicAdapter.notifyItemChanged(position, DynamicAdapter.UPDATE_DISCUSS)
                    }
                }
            }
        }
    }

    private fun onLikeMoment() {
        LiveEventBus.get<String>(LiveDataEventManager.LIKE_MOMENT_SUCCESS).observe(this) {
            for (i in dynamicAdapter.items.indices) {
                val item = dynamicAdapter.getItem(i)
                item?.apply {
                    if (item.id.toString() == it) {
                        //点赞动态
                        item.trendsIsLike = true
                        item.trendsLikeCount++
                        dynamicAdapter.notifyItemChanged(i, DynamicAdapter.UPDATE_LIKE)
                        return@observe
                    }
                }

            }
        }
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(context, true, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    page = 1
                    requestData()
                }

                override fun onLoadMore() {
                    page++
                    requestData()
                }
            })
    }


    private fun addRvScrollChangeListener() {
        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = mBinding.recyclerView.layoutManager as LinearLayoutManager
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) { //滚动停止
                    activity?.apply {
                        Glide.with(this).resumeRequests()

                        val findFirstVisibleItemPosition =
                            layoutManager.findFirstVisibleItemPosition()
                        val findLastVisibleItemPosition =
                            layoutManager.findLastVisibleItemPosition()
                        var position: Int
                        position =
                            if (findFirstVisibleItemPosition != findLastVisibleItemPosition) {
                                kotlin.random.Random.nextInt(
                                    findFirstVisibleItemPosition,
                                    findLastVisibleItemPosition
                                )
                            } else {
                                0
                            }
                        if (position <= 0) {
                            position = 0
                        }
                        val totalSize = dynamicAdapter.itemCount
                        if (position < totalSize) {
                            startPlay(position)
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                activity?.apply {
                    if (kotlin.math.abs(dy) < 150) {
                        Glide.with(this).resumeRequests()
                    } else {
                        Glide.with(this).pauseRequests()
                    }
                }

            }
        })
    }

    /**
     * 当前播放的位置
     */
    var mCurPos = -1
    fun startPlay(position: Int) {
        if (mCurPos == position) return
        if (mCurPos != -1) {
            releaseVideoView()
        }
        val videoBean = dynamicAdapter.getItem(position)
        val images = videoBean?.images
        images?.forEach {
            if (VideoUtils.isVideo(it)) {
                val viewHolder =
                    dynamicAdapter.recyclerView.findViewHolderForAdapterPosition(position) as DynamicAdapter.VH
                val proxyUrl = VideoCacheProxy.getCacheUrl(mContext, it)
                mVideoView!!.setUrl(proxyUrl)
                mVideoView?.setOnStateChangeListener(object :
                    BaseVideoView.OnStateChangeListener {
                    override fun onPlayerStateChanged(playerState: Int) {
                    }

                    override fun onPlayStateChanged(playState: Int) {
                        if (playState == VideoView.STATE_PLAYING) {
                            ViewUtils.removeViewFormParent(mVideoView)
                            val vgSingleVideo = viewHolder.binding.vgSingleVideo
                            vgSingleVideo.vgVideo.addView(mVideoView)
                        }
                    }
                })
                mVideoView!!.start()
                mCurPos = position
                return@forEach
            }

        }
    }

    private fun addOnChildAttachStateChangeListener() {
        mBinding.recyclerView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {
                val playerContainer: FrameLayout? =
                    view.findViewById(R.id.vg_video)
                if (playerContainer != null) {
                    val v = playerContainer.getChildAt(0)
                    if (v != null && v == mVideoView && !mVideoView!!.isFullScreen) {
                        releaseVideoView()
                    }
                }
            }
        })
    }

    private fun releaseVideoView() {
        mVideoView?.apply {
            this.pause()
            this.release()
            ViewUtils.removeViewFormParent(this)
            if (this.isFullScreen) {
                this.stopFullScreen()
            }
            mCurPos = -1
        }

    }

    var mVideoView: VideoView? = null
    private fun initVideoView() {
        mVideoView = VideoView(mContext)
        mVideoView?.apply {
            setPlayerBackgroundColor(Color.TRANSPARENT)
            setLooping(true)
            isMute = true
            setScreenScaleType(BaseVideoView.SCREEN_SCALE_CENTER_CROP)
            setOnStateChangeListener(object : BaseVideoView.SimpleOnStateChangeListener() {
                override fun onPlayStateChanged(playState: Int) {
                    //监听VideoViewManager释放，重置状态
                    if (playState == VideoView.STATE_IDLE) {
                        ViewUtils.removeViewFormParent(mVideoView)
                        mCurPos = -1
                    }
                }
            })
            setVideoController(null)
        }
    }

    private fun pauseVideoView() {
        mVideoView?.pause()
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    private fun pause() {
        pauseVideoView()
    }

    override fun onResume() {
        super.onResume()
        mVideoView?.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseVideoView()
    }
}
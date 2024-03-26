package cn.yanhu.dynamic.ui.activity

import android.content.Context
import android.content.Intent
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.bean.AttachParamsInfo
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.dynamic.OnClickDiscussListener
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.adapter.DiscussAdapter
import cn.yanhu.dynamic.adapter.DynamicAdapter
import cn.yanhu.dynamic.adapter.DynamicDetailHeadAdapter
import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.databinding.ActivityDynamicDetailBinding
import cn.yanhu.dynamic.manager.MomentManager
import cn.yanhu.dynamic.pop.InputDiscussPop
import cn.yanhu.dynamic.ui.DynamicViewModel
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.jeremyliao.liveeventbus.LiveEventBus


/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
class DynamicDetailActivity : BaseActivity<ActivityDynamicDetailBinding, DynamicViewModel>(
    R.layout.activity_dynamic_detail,
    DynamicViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private var dynamicId: String = ""
    private var page = 1
    private val discussAdapter by lazy {
        DiscussAdapter()
    }
    private val dynamicAdapter by lazy { DynamicAdapter() }
    private val dynamicDetailHeadAdapter by lazy { DynamicDetailHeadAdapter() }
    override fun initData() {
        dynamicId = intent.getStringExtra(IntentKeyConfig.ID).toString()
        setStatusBarStyle(false)
        initRefresh()
        helper = QuickAdapterHelper.Builder(discussAdapter)
            .build()
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        discussAdapter.setTrendsId(dynamicId)
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(0, dynamicAdapter.apply {
            this.addOnItemChildClickListener(R.id.tv_discussNum, itemChildListener)
            this.addOnItemChildClickListener(R.id.iv_discuss, itemChildListener)
        })
        helper.addBeforeAdapter(1, dynamicDetailHeadAdapter)
        requestData()
    }

    private val itemChildListener =
        BaseQuickAdapter.OnItemChildClickListener<DynamicInfo> { _, _, _ -> showDiscussInput() }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                showDeleteDynamicPop(v)
            }

        })
        mBinding.tvContent.setOnSingleClickListener {
            showDiscussInput()
        }
        discussAdapter.setOnClickTextListener(object : OnClickDiscussListener {
            override fun onClickItem(position: Int, item: DiscussInfo) {
                showDiscussReplyInput(position, item)
            }
        })
        discussAdapter.addOnItemChildLongClickListener(R.id.tv_content
        ) { _, view, position ->
            showDiscussLongClickEvent(position, view)
            false
        }
        discussAdapter.setOnItemLongClickListener { _, view, position ->
            showDiscussLongClickEvent(position, view)
            false
        }
        discussAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<DiscussInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<DiscussInfo, *>,
                view: View,
                position: Int
            ) {
                val item = discussAdapter.getItem(position) ?: return
                showDiscussReplyInput(position, item)
            }
        })
        discussAdapter.addOnItemChildClickListener(R.id.avatarView, discussItemChildListener)
        discussAdapter.addOnItemChildClickListener(R.id.tv_content, discussItemChildListener)
        discussAdapter.setReplyListener(object : DiscussAdapter.OnClickReplyListener {
            override fun onClickReply(item: DiscussInfo, position: Int) {
                //回复二级评论
                discussInfo = item
                discussInfo!!.isReply = true
                discussPosition = position
                showInput()
            }
        })
    }

    private fun showDiscussLongClickEvent(position: Int, view: View) {
        val item = discussAdapter.getItem(position)
        DialogUtils.showLongClickCopyDialog(
            view,
            item!!.info,
            object : DialogUtils.OnDeleteListener {
                override fun startDelete() {
                    //删除评论
                    showDeleteDiscussPop(item, position)
                }
            })
    }

    private fun showDeleteDiscussPop(item: DiscussInfo, position: Int) {
        val id = item.id
        MomentManager.showDeleteDiscussDialog(
            id.toString(),
            item.isSelf(),
            dynamicId,
            object :
                OnBooleanResultListener {
                override fun onSuccess() {
                    discussAdapter.removeAt(position)
                }
            })
    }

    private fun showDeleteDynamicPop(v: View?) {
        val list: MutableList<AttachParamsInfo> = mutableListOf()
        if (dynamicInfo?.isSelf() == true) {
            list.add(AttachParamsInfo("删除", cn.yanhu.baselib.R.drawable.icon_red_delete,
                itemColor = cn.yanhu.baselib.R.color.fontRedColor, type = TYPE_DELETE_DYNAMIC))
        } else {
            if (AppCacheManager.isAdmin) {
                list.add(AttachParamsInfo("删除", cn.yanhu.baselib.R.drawable.icon_red_delete,
                    itemColor = cn.yanhu.baselib.R.color.fontRedColor, type = TYPE_DELETE_DYNAMIC))
            }
            list.add(AttachParamsInfo("举报", cn.yanhu.commonres.R.drawable.icon_report_black,
                itemColor = cn.yanhu.baselib.R.color.fontTextColor, type = TYPE_REPORT))
        }
        DialogUtils.showCustomAttachViewPop(
            mContext,
            v!!,
            list,
            offsetX = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_15),
            onSelectListener = { _, attachParamsInfo ->
                attachParamsInfo?.apply {
                    if (type == TYPE_DELETE_DYNAMIC) {
                        MomentManager.showDeleteMomentDialog(mContext,
                            dynamicId,
                            dynamicInfo?.isSelf() == true,
                            object : OnBooleanResultListener {
                                override fun onSuccess() {
                                    finish()
                                }
                            })
                    } else {
                        //举报
                        RouteIntent.lunchReportPage()
                    }
                }
            })
    }

    private val discussItemChildListener =
        object : BaseQuickAdapter.OnItemChildClickListener<DiscussInfo> {
            override fun onItemClick(
                adapter: BaseQuickAdapter<DiscussInfo, *>,
                view: View,
                position: Int
            ) {
                val item = discussAdapter.getItem(position) ?: return
                if (view.id == R.id.tv_content) {
                    showDiscussReplyInput(position, item)
                } else {
                    RouteIntent.lunchPersonHomePage(item.userId)
                }
            }

        }

    override fun requestData() {
        super.requestData()
        mViewModel.getDynamicDetail(dynamicId, page)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onLikeMoment()
        onUnlikeMoment()
        onGetDetailListener()
        onDiscussCallBack()
        onDeleteDiscussSuccess()
    }


    private fun onDiscussCallBack() {
        LiveEventBus.get<DiscussInfo>(LiveDataEventManager.DISCUSS_SUCCESS).observe(this) {
            dynamicInfo!!.trendsCommentCount++
            dynamicDetailHeadAdapter.item = dynamicInfo
            dynamicAdapter[0] = dynamicInfo!!
            if (discussInfo != null) {
                //回复评论成功
                discussAdapter.notifyItemChanged(discussPosition, it)
            } else {
                //评价动态成功
                discussAdapter.add(0, it)
                mBinding.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun onDeleteDiscussSuccess() {
        LiveEventBus.get<String>(LiveDataEventManager.DELETE_DISCUSS_SUCCESS).observe(this) {
            dynamicInfo!!.trendsCommentCount--
            dynamicDetailHeadAdapter.item = dynamicInfo
            dynamicAdapter[0] = dynamicInfo!!
        }
    }

    private fun onGetDetailListener() {
        mViewModel.dynamicDetailObserver.observe(this) { it ->
            parseState(it, {
                val trendsComments = it.trendsComments
                dynamicInfo = it.trendsInfo
                if (page == 1) {
                    if (dynamicAdapter.items.isNotEmpty()) {
                        dynamicAdapter[0] = dynamicInfo!!
                    } else {
                        dynamicAdapter.add(dynamicInfo!!)
                    }
                    dynamicDetailHeadAdapter.item = dynamicInfo
                    discussAdapter.submitList(trendsComments)
                } else {
                    discussAdapter.addAll(trendsComments)
                }
                setDataLoadFinish(page, trendsComments.size, mBinding.refreshLayout)
            }, {
                finish()
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    private var discussInfo: DiscussInfo? = null
    private var dynamicInfo: DynamicInfo? = null
    private var discussPosition: Int = -1
    private fun showDiscussInput() {
        discussInfo = null
        showInput()
    }

    private fun showDiscussReplyInput(position: Int, item: DiscussInfo) {
        discussPosition = position
        discussInfo = item
        showInput()
    }

    private fun showInput() {
        if (discussInfo != null) {
            if (discussPosition == -1) {
                return
            }
            val item = discussAdapter.getItem(discussPosition) ?: return
            if (discussInfo!!.isReply) {
                //回复二级评论
                InputDiscussPop.showInputDialog(
                    mContext,
                    dynamicId,
                    commentId = item.id.toString(),
                    discussInfo!!.id.toString(),
                    discussInfo!!.userId,
                    discussInfo!!.nickName
                )
            } else {
                //回复一级评论
                InputDiscussPop.showInputDialog(
                    mContext,
                    dynamicId,
                    commentId = item.id.toString(),
                    userName = discussInfo!!.nickName
                )
            }
        } else {
            //评论动态
            InputDiscussPop.showInputDialog(mContext, dynamicId, userName = dynamicInfo?.nickName)
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
            .initRefresh(mContext, true, mBinding.refreshLayout, object : IRefreshCallBack {
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


    companion object {
        const val TYPE_DELETE_DYNAMIC = 1
        const val TYPE_REPORT = 2
        fun lunch(context: Context, id: String) {
            val intent = Intent(context, DynamicDetailActivity::class.java)
            intent.putExtra(IntentKeyConfig.ID, id)
            context.startActivity(intent)
        }
    }
}
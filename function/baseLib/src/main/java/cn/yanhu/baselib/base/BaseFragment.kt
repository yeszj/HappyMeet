package cn.yanhu.baselib.base

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.yanhu.baselib.R
import cn.yanhu.baselib.callBack.IBaseView
import cn.yanhu.baselib.loadinglayout.EmptyCallBack
import cn.yanhu.baselib.loadinglayout.ErrorCallback
import cn.yanhu.baselib.loadinglayout.LoadingCallBack
import cn.yanhu.baselib.loadinglayout.LoadingHasContentCallBack
import cn.yanhu.baselib.refresh.MyRefreshLayout
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.zj.netrequest.BaseViewModel
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import java.util.*

@Suppress("DEPRECATION")
abstract class BaseFragment<DB : ViewDataBinding, VM : BaseViewModel>(
    private val layoutId: Int,
    private val cls: Class<VM>,
) : Fragment(), IBaseView {

    protected val mContext by lazy {
        requireActivity()
    }

    protected lateinit var mBinding: DB
        private set

    protected lateinit var mViewModel: VM

    protected lateinit var loadService: LoadService<*>

    //Fragment的View加载完毕的标记
    private var isViewCreated = false


    //页面是否已经初始化
    protected var isInit = false
    protected open val footView by lazy {
        NoMoreDataFootView(mContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mViewModel = createViewModel()
        isInit = true
        initLoadService()
        registerNecessaryObserver()
        initData()
        initRefresh()
        initListener()
        //       return mBinding.root
        return loadService.loadLayout

    }

    open fun initLoadService() {
        loadService = LoadSir.getDefault().register(mBinding.root) {
            onReload()
        }
    }

    fun initCustomLoadingLoad(loadCallBack: Callback): LoadSir {
        loadingCallBack = loadCallBack::class.java
        return LoadSir.Builder()
            .addCallback(ErrorCallback())
            .addCallback(EmptyCallBack())
            .addCallback(LoadingHasContentCallBack())
            .setDefaultCallback(SuccessCallback::class.java)
            .addCallback(loadCallBack)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        lazyLoad()
    }

    protected open fun lazyLoad() {
        //这里进行双重标记判断,是因为setUserVisibleHint会多次回调,并且会在onCreateView执行前回调,必须确保onCreateView加载完毕且页面可见,才加载数据
        if (isViewCreated && mResume) {
            requestData()
            //数据加载完毕,恢复标记,防止重复加载
            isViewCreated = false
        }
    }



    var isAttachActivity: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        isAttachActivity = true
    }

    override fun onDetach() {
        super.onDetach()
        isAttachActivity = false
    }


    protected open fun onReload() {
        requestData()
    }

    protected open fun initRefresh() {}
    private fun createViewModel(): VM {
        return ViewModelProvider(this)[cls]
    }

    protected open fun initListener() {
    }


    /**
     * 网络请求数据
     */
    protected open fun requestData() {}

    protected abstract fun initData()


    override fun showContent() {
        loadService.showSuccess()
    }

    override fun showEmpty() {
        loadService.showCallback(EmptyCallBack::class.java)
    }

    private var loadingCallBack: Any? = LoadingCallBack::class.java
    override fun showLoading(showContent: Boolean, content: String) {
        if (showContent) {
            showLoadingHasContent(content)
        } else {
            showPageLoading()
        }
    }

    open fun showPageLoading() {
        loadService.showCallback(loadingCallBack as Class<Callback>)
    }

    private fun showLoadingHasContent(content: String) {
        loadService.setCallBack(LoadingHasContentCallBack::class.java
        ) { _, view ->
            val tvMyDialog = view?.findViewById<TextView>(R.id.tv_MyDialog)
            tvMyDialog?.text = content
        }
        loadService.showCallback(LoadingHasContentCallBack::class.java)
    }


    override fun showError(content: String) {
        loadService.setCallBack(ErrorCallback::class.java
        ) { _, view ->
            val tvRetry = view?.findViewById<TextView>(R.id.tv_retry)
            tvRetry?.text = content
            val tvOperate = view?.findViewById<TextView>(R.id.tv_operate)
            tvOperate?.setOnSingleClickListener { onReload() }
        }
        loadService.showCallback(ErrorCallback::class.java)
    }

    open fun beginRefreshing(bgRefresh: MyRefreshLayout) {
        bgRefresh.autoRefresh()
    }

    open fun endRefreshing(bgRefresh: MyRefreshLayout) {
        bgRefresh.finishRefresh()
    }

    open fun endLoadingMore(bgRefresh: MyRefreshLayout) {
        bgRefresh.finishLoadMore()
    }

    open fun endLoad(pageNum: Int, bgRefresh: MyRefreshLayout) {
        if (pageNum == 1) {
            endRefreshing(bgRefresh)
        } else {
            endLoadingMore(bgRefresh)
        }
    }

    open fun getEmptyView(): NoMoreDataFootView {
        footView.setCustomBackGroundColor(R.color.transparent)
        footView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        return footView
    }

    open fun setDataLoadFinish(pageNum: Int, size: Int, bgRefresh: MyRefreshLayout) {
        if (pageNum == 1) {
            if (size <= 0) {
                bgRefresh.finishLoadMoreWithNoMoreData()
            }
            endRefreshing(bgRefresh)
        } else {
            if (size <= 0) {
                bgRefresh.finishLoadMoreWithNoMoreData()
            } else {
                endLoadingMore(bgRefresh)
            }
        }
    }

    open fun setDataLoadFinish(size: Int, bgRefresh: MyRefreshLayout) {
        endRefreshing(bgRefresh)
    }

    /**
     * 注册 UI 事件
     */
    private var isShowContent: Boolean = true
    protected open fun registerNecessaryObserver() {
        mViewModel.loadingChange.showDialog.observe(this) {
            isShowContent = it
            showLoading(it)
        }
        mViewModel.loadingChange.dismissDialog.observe(this) {
            if (it) {
                showContent()
            } else {
                if (isShowContent) {
                    showContent()
                } else {
                    showError()
                }
            }
        }
        mViewModel.loadingChange.errorMsgDialog.observe(this) {
            if (!TextUtils.isEmpty(it)) {
                showError(content = it)
            }
        }
    }

    /**
     * @desc 添加fragment
     * @author zhengjun
     * @created at 2019/1/8 14:32
     */
    open fun addFragment(fragment: Fragment?) {
        childFragmentManager.beginTransaction().add(R.id.containerLayout, fragment!!)
            .commitAllowingStateLoss()
    }

    open fun removeFragment(fragment: Fragment?) {
        fragment?.apply {
            childFragmentManager.beginTransaction().remove(fragment)
                .commitAllowingStateLoss()
        }
    }

    /**
     * @desc 替换fragment
     * @author zhengjun
     * @created at 2019/1/8 14:35
     */
    open fun replaceFragment(oldFragment: Fragment?, newFragment: Fragment) {
        val beginTransaction = childFragmentManager.beginTransaction()
        beginTransaction.setCustomAnimations(
            R.anim.slide_right_in,
            R.anim.slide_left_out,
            R.anim.slide_left_in,
            R.anim.slide_right_out
        )
        if (newFragment.isAdded) {
            beginTransaction.show(newFragment)
        } else {
            beginTransaction.add(R.id.containerLayout, newFragment)
        }
        if (oldFragment != null) {
            beginTransaction.hide(oldFragment)
        }
        beginTransaction.commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        mResume = true
        lazyLoad()
    }


    private var mResume = false // 标记onResume是否被刚刚调用

    // 自定义方法，用于判断fragment真实展现，不要用其提供的isVisible()方法
    open fun isRealVisible(): Boolean {
        return userVisibleHint && mResume
    }

}
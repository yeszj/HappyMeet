package cn.yanhu.baselib.base

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import cn.yanhu.baselib.loadinglayout.LoadingManager
import cn.yanhu.baselib.refresh.MyRefreshLayout
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.DisplayUtils
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.view.TitleBar
import cn.zj.netrequest.BaseViewModel
import com.blankj.utilcode.util.LanguageUtils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir


/**
 * activity基类(勿修改)
 */
abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel>(
    private val layoutId: Int,
    private val cls: Class<VM>,
) : AppCompatActivity(), IBaseView {

    protected val mContext by lazy {
        this
    }
    protected var isRealShow: Boolean = false

    protected lateinit var mBinding: DB
        private set

     lateinit var mViewModel: VM

    protected lateinit var loadService: LoadService<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.hjq.toast.ToastUtils.init(mContext.application)
        //设置禁止截屏、录屏标志
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setEnterAnim()
        setOrientation()
        mBinding = DataBindingUtil.setContentView(this, layoutId)
        mViewModel = createViewModel()
        initLoadService()
        reloadWhenDestroy(savedInstanceState)
        initStatusBar()
        DisplayUtils.initScreen(this)
        registerNecessaryObserver()
        initListener()
        initData()
        initRefresh()
        addBackPressListener()
    }
    open fun setTitleMarginTop(view: View?) {
        if (view != null) {
            val statusBarHeight = StatusBarUtil.getStatusBarHeight(mContext)
            ViewUtils.setMarginTop(view, statusBarHeight)
        }
    }
    open fun initBeforeContentView(){}

    private fun addBackPressListener() {
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                back()
            }
        }
        onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }


    open fun setOrientation() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    open fun initLoadService() {
        loadService = LoadSir.getDefault().register(this) {
            onReload()
        }
    }

    private var loadingCallBack: Any? = LoadingCallBack::class.java
    fun initCustomLoadingLoad(loadCallBack: Callback): LoadSir {
        loadingCallBack = loadCallBack::class.java
        return LoadingManager.initCustomLoading(loadCallBack)
    }

    override fun attachBaseContext(newBase: Context?) {
        val attachBaseContext = LanguageUtils.attachBaseContext(newBase)
        val configuration: Configuration = attachBaseContext.resources.configuration
        configuration.fontScale = 1f
        super.attachBaseContext(attachBaseContext)
    }


    protected open fun setEnterAnim() {
        overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out)
    }

    protected open fun reloadWhenDestroy(savedInstanceState: Bundle?) {}

    protected open fun initStatusBar() {
        mBinding.root.fitsSystemWindows = true
        StatusBarUtil.setWindowStatusBarColor(mContext, R.color.white)
        setStatusBarStyle()
    }

    open fun setStatusBarToWhite() {
        mBinding.root.fitsSystemWindows = true
        StatusBarUtil.setWindowStatusBarColor(mContext,R.color.white)
        setStatusBarStyle(false)
    }

    protected open fun setFullScreenStatusBar(isTabActivity: Boolean = false) {
        mBinding.root.fitsSystemWindows = false
        StatusBarUtil.setStatusBarFullTransparent(this)
        if (!isTabActivity) {
            val titleBar = mBinding.root.findViewById<TitleBar>(R.id.titleBar)
            val statusBarHeight = StatusBarUtil.getStatusBarHeight(mContext)
            ViewUtils.setPaddingTop(titleBar, statusBarHeight)
        }
    }

    protected open fun onReload() {
        requestData()
    }

    protected open fun initRefresh() {}

    private fun createViewModel(): VM {
        return ViewModelProvider(this)[cls]
    }

    /**
     * 设置加载布局区域(默认为activity对应xml的根布局)
     */
    protected open fun loadingLayout(): Any {
        return this
    }

    /**
     * 注册必要的观察者
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
     * 初始化监听器
     */
    protected open fun initListener() {}

    /**
     * 网络请求数据
     */
    protected open fun requestData() {}

    /**
     * 初始化数据
     */
    abstract fun initData()


    //  --------------statusbar start--------------
    private var systemType = 0

    /**
     * 设置状态栏风格
     * @param isDark 默认为true，暗色风格，文字为白色(默认)
     */
    fun setStatusBarStyle(isDark: Boolean = true) {
        if (isDark) {
            StatusBarUtil.StatusBarDarkMode(this, systemType)
        } else {
            systemType = StatusBarUtil.StatusBarLightMode(this)
        }
    }


    //  --------------statusbar end--------------

    //--------------loadingLayout start--------------
    override fun showContent() {
        loadService.showSuccess()
    }

    override fun showEmpty() {
        loadService.showCallback(EmptyCallBack::class.java)
    }

    protected open val footView by lazy {
        NoMoreDataFootView(mContext)
    }

    open fun getEmptyView(): NoMoreDataFootView {
        footView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        return footView
    }

    override fun showLoading(showContent: Boolean, content: String) {
        if (showContent) {
            loadService.setCallBack(LoadingHasContentCallBack::class.java
            ) { _, view ->
                val tvMyDialog = view?.findViewById<TextView>(R.id.tv_MyDialog)
                tvMyDialog?.text = content
            }
            loadService.showCallback(LoadingHasContentCallBack::class.java)
        } else {
            loadService.showCallback(loadingCallBack as Class<Callback>)
        }
    }

    override fun showError(content: String) {
        loadService.setCallBack(ErrorCallback::class.java
        ) { _, view ->
            val tvRetry = view?.findViewById<TextView>(R.id.tv_retry)
            tvRetry?.text = content
        }
        loadService.showCallback(ErrorCallback::class.java)
    }
    //--------------loadingLayout end--------------
    open fun back() {
        finish()
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

    open fun setDataLoadFinish(pageNum: Int, size: Int, bgRefresh: MyRefreshLayout) {
        if (pageNum == 1) {
            if (size > 0) {
                footView.footViewState(NoMoreDataFootView.FOOT_HIDE)
            } else {
                footView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
                bgRefresh.finishLoadMoreWithNoMoreData()
            }
            endRefreshing(bgRefresh)
        } else {
            if (size <= 0) {
                footView.footViewState(NoMoreDataFootView.FOOT_NO_MORE)
                bgRefresh.finishLoadMoreWithNoMoreData()
            } else {
                footView.footViewState(NoMoreDataFootView.FOOT_HIDE)
                endLoadingMore(bgRefresh)
            }
        }
    }

    open fun setDataLoadFinish(size: Int, bgRefresh: MyRefreshLayout) {
        if (size > 0) {
            footView.footViewState(NoMoreDataFootView.FOOT_NO_MORE)
        } else {
            footView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        }
        endRefreshing(bgRefresh)
    }

    open fun endLoad(pageNum: Int, bgRefresh: MyRefreshLayout) {
        if (pageNum == 0) {
            endRefreshing(bgRefresh)
        } else {
            endLoadingMore(bgRefresh)
        }
    }

    fun goLaunch(context: Context, tClass: Class<*>?) {
        val intent = Intent(context, tClass)
        context.startActivity(intent)
    }

    /**
     * @desc 添加fragment
     * @author zhengjun
     * @created at 2019/1/8 14:32
     */
    open fun addFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction().add(R.id.containerLayout, fragment!!)
            .commitAllowingStateLoss()
    }

    open fun removeFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction().remove(fragment!!)
            .commitAllowingStateLoss()
    }

    /**
     * @desc 替换fragment
     * @author zhengjun
     * @created at 2019/1/8 14:35
     */
    open fun replaceFragment(oldFragment: Fragment?, newFragment: Fragment,isBack:Boolean) {
        val beginTransaction = supportFragmentManager.beginTransaction()
        if (newFragment.isAdded) {
            if (isBack) {
                beginTransaction.setCustomAnimations(
                    R.anim.slide_left_in,
                    R.anim.slide_right_out
                )
            } else {
                beginTransaction.setCustomAnimations(
                    R.anim.slide_right_in,
                    R.anim.slide_left_out
                )
            }
            beginTransaction.show(newFragment)
        } else {
            beginTransaction.setCustomAnimations(
                R.anim.slide_right_in,
                R.anim.slide_left_out
            )
            beginTransaction.add(R.id.containerLayout, newFragment)
        }
        if (oldFragment != null) {
            beginTransaction.hide(oldFragment)
        }
        beginTransaction.commitAllowingStateLoss()
    }

    /**
     * @desc 显示已经显示过的fragment
     * @author zhengjun
     * @created at 2019/1/8 14:36
     */
    open fun showFragment(hideFragment: Fragment, showFragment: Fragment) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
           R.anim.slide_left_in,
           R.anim.slide_right_out,
           R.anim.slide_right_in,
            R.anim.slide_left_out
        ).show(showFragment).hide(hideFragment).commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        isRealShow = true
    }

    override fun onPause() {
        super.onPause()
        isRealShow = false
        if (isFinishing && !isDestroy) {
            isDestroy = true
            loadingCallBack = null
            exactDestroy()
        }
    }

    private var isDestroy = false
    override fun onDestroy() {
        super.onDestroy()
        if (!isDestroy) {
            isDestroy = true
            loadingCallBack = null
            exactDestroy()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out)
    }

    open fun exactDestroy() {
    }
}
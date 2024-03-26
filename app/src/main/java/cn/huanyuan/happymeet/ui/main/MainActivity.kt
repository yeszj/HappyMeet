package cn.huanyuan.happymeet.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.TabEntity
import cn.huanyuan.happymeet.databinding.ActivityMainBinding
import cn.huanyuan.happymeet.ui.main.tab_blinddate.TabBlindDateFrg
import cn.huanyuan.happymeet.ui.main.tab_samecity.TabSameCityFrg
import cn.huanyuan.happymeet.ui.main.tab_msg.TabMessageFrg
import cn.huanyuan.happymeet.ui.main.tab_my.TabMineFrg
import cn.huanyuan.happymeet.ui.main.tab_wallet.TabWalletFrg
import cn.yanhu.agora.manager.BeautySDKManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.base.BaseTabAdapter
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.loading.MainLoadingCallBack
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chaychan.library.BottomBarItem
import kotlin.system.exitProcess


/**
 * @author: zhengjun
 * created: 2024/1/30
 * desc:
 */
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main,
    MainViewModel::class.java
) {
    private var titleList = mutableListOf<String>()
    private var mFragmentList = mutableListOf<Fragment>()
    private var tabList: MutableList<TabEntity> = mutableListOf()
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        ThreadUtils.getMainHandler().post {
            showLoading(false)
        }
        BeautySDKManager.sharedInstance().downloadBundle()
        mViewModel.getMainTabInfo()
    }

    override fun initLoadService() {
        val loadSir = initCustomLoadingLoad(MainLoadingCallBack())
        loadService = loadSir.register(mBinding.root) {
            onReload()
        }
    }

    override fun registerNecessaryObserver() {
        onGetTabSuccess()
    }

    private fun onGetTabSuccess() {
        mViewModel.tabInfoObservable.observe(this) { it ->
            parseState(it, {
                tabList = it
                if (mFragmentList.size <= 0) {
                    initFrg()
                }
                showContent()
            })
        }
    }

    private fun initFrg() {
        if (mFragmentList.size > 0) {
            clearAllFrgManager()
        }
        tabList.forEach {
            when (it.id) {
                1 -> {
                    mFragmentList.add(TabSameCityFrg())
                }

                2 -> {
                    mFragmentList.add(TabMessageFrg())
                }

                3 -> {
                    mFragmentList.add(TabBlindDateFrg())
                }

                4 -> {
                    mFragmentList.add(TabWalletFrg())
                }

                5 -> {
                    mFragmentList.add(TabMineFrg())
                }
            }
            mBinding.tabLayout.addItem(createBottomBarItem(it))
        }
        bindTabToVp()
    }

    private fun createBottomBarItem(tabEntity: TabEntity): BottomBarItem {
        val bottomBarItem = BottomBarItem.Builder(this).titleTextSize(12).titleSelectedColor(
            cn.yanhu.baselib.R.color.colorMain
        ).titleNormalColor(cn.yanhu.baselib.R.color.fontTextColor)
            .unreadNumThreshold(99)
            .unreadTextColor(R.color.white)
            .iconHeight(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            .iconWidth(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            //还有很多属性，详情请查看Builder里面的方法
            .create(
                cn.yanhu.baselib.R.drawable.svg_black_back,
                cn.yanhu.baselib.R.drawable.svg_black_back,
                tabEntity.name
            )
        GlideUtils.loadAsDrawable(
            mContext,
            tabEntity.normalIcon,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    bottomBarItem.setNormalIcon(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        GlideUtils.loadAsDrawable(
            mContext,
            tabEntity.selectIcon,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    bottomBarItem.setSelectedIcon(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        return bottomBarItem
    }

    private fun clearAllFrgManager() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        if (mFragmentList.size > 0) {
            for (fragment in mFragmentList) {
                ft.remove(fragment)
            }
            ft.commitNow()
            mFragmentList.clear()
            mBinding.tabLayout.removeAllViews()
        }
    }

    private fun bindTabToVp() {
        val tabAdapter = BaseTabAdapter(supportFragmentManager, titleList, mFragmentList)
        mBinding.viewPager.adapter = tabAdapter
        mBinding.tabLayout.setViewPager(mBinding.viewPager)
        mBinding.viewPager.offscreenPageLimit = mFragmentList.size
        mBinding.tabLayout.setOnItemSelectedListener { _, _, _ ->
            KeyboardUtils.hideSoftInput(
                mContext
            )
        }
        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position==getTabSameCityPosition()){
                    LiveDataEventManager.sendLiveDataMessage(
                        LiveDataEventManager.SWITCH_TO_SAME_CITY,
                        position
                    )
                }
            }
        })
        mBinding.viewPager.currentItem = 2
    }

    private fun getTabSameCityPosition():Int{
        for (i in 0..<mFragmentList.size){
            val fragment = mFragmentList[i]
            if (fragment is TabSameCityFrg){
                return i
            }
        }
        return -1
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            val isExit = CommonUtils.exitApp(keyCode)
            if (!isExit) {
                ActivityUtils.finishAllActivities()
                exitProcess(0)
            } else {
                true
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        fun lunch(context: Activity, bundle: Bundle?=null) {
            val intent = Intent(context, MainActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivity(intent)
        }
    }
}
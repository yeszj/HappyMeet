package cn.huanyuan.sweetlove.ui.userinfo

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityUserSearchBinding
import cn.huanyuan.sweetlove.ui.userinfo.adapter.UserSearchAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/12
 * desc:
 */
class UserSearchActivity : BaseActivity<ActivityUserSearchBinding, UserViewModel>(
    R.layout.activity_user_search,
    UserViewModel::class.java
) {
    private val userAdapter by lazy { UserSearchAdapter() }
    override fun initData() {
        setStatusBarStyle(false)
        userAdapter.stateView = getEmptyView()
        mBinding.rvUser.adapter = userAdapter
        userAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<UserDetailInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<UserDetailInfo, *>,
                view: View,
                position: Int
            ) {
                val item = userAdapter.getItem(position) ?: return
                if (item.roomId > 0) {
                    LiveRoomManager.toLiveRoomPage(mContext,item.roomId.toString())
                } else {
                    RouteIntent.lunchPersonHomePage(item.userId)
                }
            }

        })
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvSearch.setOnSingleClickListener {
            requestData()
        }
        mBinding.etContent.setOnEditorActionListener { v, actionId, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                return@setOnEditorActionListener true
            }
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                requestData()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun requestData() {
        super.requestData()
        val content = mBinding.etContent.text.toString().trim()
        mViewModel.searchUserList(content)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.searchUserObservable.observe(this) { it ->
            parseState(it, {
                mBinding.tvResult.visibility = View.VISIBLE
                userAdapter.isStateViewEnable = it.size <= 0
                userAdapter.submitList(it)
            })
        }
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, UserSearchActivity::class.java))
        }
    }
}
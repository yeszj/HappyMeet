package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.RoomSelectPkUserAdapter
import cn.yanhu.agora.bean.PkSeatUserInfo
import cn.yanhu.agora.databinding.PopSelectPkUserBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
@SuppressLint("ViewConstructor")
class SelectPkUserPop(
    context: Context,
    val isSelectRedUser: Boolean,
    var userList: MutableList<PkSeatUserInfo>,
    val maxCount: Int,
    val onSelectUserListener: OnSelectUserListener
) : BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            isSelectRedUser: Boolean,
            userList: MutableList<PkSeatUserInfo>,
            maxCount: Int,
            onSelectUserListener: OnSelectUserListener
        ): SelectPkUserPop {
            val matchPop =
                SelectPkUserPop(mContext, isSelectRedUser, userList, maxCount, onSelectUserListener)
            val builder = XPopup.Builder(mContext)
                .enableDrag(false)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_select_pk_user
    }

    private lateinit var mBinding: PopSelectPkUserBinding
    private val mAdapter by lazy { RoomSelectPkUserAdapter(isSelectRedUser) }
    private var selectCount = 0
    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSelectPkUserBinding.bind(popupImplView)
        mBinding.rvUser.adapter = mAdapter
        resetLayoutManger()
        mAdapter.submitList(userList)
        if (isSelectRedUser){
            mBinding.tvTitle.text = "选择红方成员"
        }else{
            mBinding.tvTitle.text = "选择蓝方成员"
        }
        selectCount = mAdapter.getSelectCount()
        mBinding.tvCount.text = "$selectCount/$maxCount"
        mBinding.ivDismiss.setOnSingleClickListener { dismiss() }
        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position) ?: return@setOnItemClickListener
            item.selectStatus = if (item.selectStatus == 0) {
                if (selectCount >= maxCount) {
                    showToast("最多可选择${maxCount}个成员")
                    return@setOnItemClickListener
                }
                if (isSelectRedUser) {
                    1
                } else {
                    2
                }
            } else {
                if (isSelectRedUser){
                    if (item.selectStatus==1){
                        0
                    }else{
                        showToast("已在蓝方队伍")
                        return@setOnItemClickListener
                    }
                }else{
                    if (item.selectStatus==2){
                        0
                    }else{
                        showToast("已在红方队伍")
                        return@setOnItemClickListener
                    }
                }
            }
            mAdapter.notifyItemChanged(position)
            selectCount = mAdapter.getSelectCount()
            mBinding.tvCount.text = "$selectCount/$maxCount"
        }
        mBinding.tvSure.setOnSingleClickListener {
            if (selectCount == 0) {
                showToast("请选择需要PK的成员")
            } else {
                onSelectUserListener.onSelectUser(mAdapter.getSelectUser())
            }
        }
    }

    private fun resetLayoutManger() {
        val manager = mBinding.rvUser.layoutManager as GridLayoutManager
        manager.spanCount = if (userList.size >= 3) 3 else userList.size
    }

    fun refreshSeatList(infos: MutableList<PkSeatUserInfo>) {
        userList = infos
        resetLayoutManger()
        mAdapter.submitList(infos)
    }

    interface OnSelectUserListener {
        fun onSelectUser(userList: MutableList<PkSeatUserInfo>)
    }
}
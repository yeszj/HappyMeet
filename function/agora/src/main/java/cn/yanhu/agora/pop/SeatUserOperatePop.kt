package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopSeatUserOperateBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
@SuppressLint("ViewConstructor")
class SeatUserOperatePop(
    context: Context,
    val userInfo: UserDetailInfo,
    private val onOperateUserListener: OnOperateUserListener
) : BottomPopupView(context) {
    private lateinit var mBinding: PopSeatUserOperateBinding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_seat_user_operate
    }

    override fun onCreate() {
        super.onCreate()
        mBinding = PopSeatUserOperateBinding.bind(popupImplView)
        mBinding.userInfo = userInfo
        mBinding.isSelf = userInfo.userId == AppCacheManager.userId
        mBinding.ivAvatar.setOnSingleClickListener {
            RouteIntent.lunchPersonHomePage(userId = userInfo.userId)
        }
        mBinding.tvAlt.setOnSingleClickListener {
            dismiss()
            onOperateUserListener.onAlt(userInfo)
        }
        mBinding.ivSendGift.setOnSingleClickListener {
            dismiss()
            onOperateUserListener.onSendGift(userInfo)
        }
        mBinding.ivAddFriend.setOnSingleClickListener {
            dismiss()
            onOperateUserListener.onAddFriend(userInfo)
        }
        mBinding.tvReport.setOnSingleClickListener {
            RouteIntent.lunchReportPage(userInfo.userId)
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userInfo: UserDetailInfo,
            onOperateUserListener: OnOperateUserListener
        ): SeatUserOperatePop {
            val matchPop = SeatUserOperatePop(mContext, userInfo, onOperateUserListener)
            val builder =
                XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    interface OnOperateUserListener {
        fun onSendGift(userInfo: UserDetailInfo)
        fun onAlt(userInfo: UserDetailInfo)
        fun onAddFriend(userInfo: UserDetailInfo)
    }
}
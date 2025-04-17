package cn.yanhu.commonres.pop

import android.content.Context
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.PopRewardShowBinding
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/4/11
 * desc:
 */
class RewardShowPop(context: Context, private val rewardInfo: RoseRechargeResponse.RewardInfo) :
    CenterPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            rewardInfo: RoseRechargeResponse.RewardInfo
        ): RewardShowPop {
            val remarkNameTipsDialog = RewardShowPop(context, rewardInfo)
            val builder = XPopup.Builder(context)
            builder
                .asCustom(remarkNameTipsDialog).show()
            return remarkNameTipsDialog
        }
    }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_reward_show
    }

    private lateinit var mBinding: PopRewardShowBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRewardShowBinding.bind(popupImplView)
        mBinding.rewardInfo = rewardInfo
        mBinding.tvSure.setOnSingleClickListener {
            dismiss()
        }
    }
}
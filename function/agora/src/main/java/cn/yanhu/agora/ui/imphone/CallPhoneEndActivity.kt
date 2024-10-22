package cn.yanhu.agora.ui.imphone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityCallPhoneEndBinding
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.commonres.config.IntentKeyConfig

/**
 * @author: zhengjun
 * created: 2024/10/15
 * desc:
 */
class CallPhoneEndActivity : BaseActivity<ActivityCallPhoneEndBinding, ImPhoneViewModel>(
    R.layout.activity_call_phone_end,
    ImPhoneViewModel::class.java
) {
    @SuppressLint("SetTextI18n")
    override fun initData() {
        setFullScreenStatusBar(true)
        val type = intent.getIntExtra(IntentKeyConfig.TYPE, 0)
        if (type == LiveRoomManager.HOUSE_CALL_OFF) {
            mBinding.liveRoomEndTxtTwo.text = "对方已挂断，通话结束"
        } else if (type == LiveRoomManager.HOUSE_CALL_PRICE_OFF) {
            mBinding.liveRoomEndTxtTwo.text = "通话所需玫瑰余额不足\n" + "已结束"
        }
        mBinding.liveRoomEndOff.setOnClickListener { v -> finish() }
    }

    companion object {
        fun lunch(context: Context, type: Int) {
            val intent = Intent(context, CallPhoneEndActivity::class.java)
            intent.putExtra(IntentKeyConfig.TYPE, type)
            context.startActivity(intent)
        }
    }
}
package cn.huanyuan.sweetlove.ui.wallet.bank

import android.content.Context
import android.content.Intent
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityBindBankBinding
import cn.huanyuan.sweetlove.ui.wallet.WalletViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2025/10/13
 * desc:
 */
class BindBankActivity : BaseActivity<ActivityBindBankBinding, WalletViewModel>(
    R.layout.activity_bind_bank,
    WalletViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        val realName = intent.getStringExtra(IntentKeyConfig.DATA)
        mBinding.etRealName.text = realName
        mBinding.btnAuth.setOnSingleClickListener {
            val bankCard = mBinding.etBankCard.text.toString()
            if (isBankValid(bankCard)){
                mViewModel.bindBank(bankCard)
            }else{
                showToast("请输入正确的银行卡号")
            }
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.bindBankResultLivedata.observe(this) {
            parseState(it,{
                showToast("提交成功")
                LiveEventBus.get<Boolean>(EventBusKeyConfig.BINDBANKSUCCESS).post(true)
                finish()
            })
        }
    }

    companion object{
        fun lunch(context: Context,realName: String){
            val intent = Intent(context, BindBankActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA,realName)
            context.startActivity(intent)
        }
    }


    /**
     * 基础格式校验
     */
    private fun isBankValid(cardNumber: String?): Boolean {
        if (cardNumber == null || cardNumber.trim { it <= ' ' }.isEmpty()) {
            return false
        }

        // 移除所有空格和特殊字符
        val cleanNumber = cardNumber.replace("[\\s-]".toRegex(), "")


        // 检查是否全为数字
        if (!cleanNumber.matches("\\d+".toRegex())) {
            return false
        }

        // 检查长度（国内银行卡号通常16-19位）
        val length = cleanNumber.length
        return length >= 16 && length <= 19
    }

}
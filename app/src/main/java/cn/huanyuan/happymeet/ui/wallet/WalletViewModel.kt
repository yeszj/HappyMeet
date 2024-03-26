package cn.huanyuan.happymeet.ui.wallet

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.WalletRecordResponse
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.commonres.bean.response.RoseExchangeResponse
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.yanhu.commonres.bean.response.WithdrawResponse
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class WalletViewModel : BaseViewModel() {
    val rechargeInfoLivedata = MutableLiveData<ResultState<RoseRechargeResponse>>()
    val withdrawalInfoLivedata = MutableLiveData<ResultState<WithdrawResponse>>()
    val withdrawalResultLivedata = MutableLiveData<ResultState<Boolean>>()
    val exchangeInfoLivedata = MutableLiveData<ResultState<RoseExchangeResponse>>()
    val exchangeResultLivedata = MutableLiveData<ResultState<String>>()
    val walletRecordLivedata = MutableLiveData<ResultState<WalletRecordResponse>>()
    fun getRechargeInfo() {
        request({ rxApi.getRechargeInfo() }, rechargeInfoLivedata, true)
    }

    fun getRoseExchangeInfo() {
        request({ rxApi.getRoseExchangeInfo() }, exchangeInfoLivedata, true)
    }

    fun getWithdrawalInfo() {
        request({ rxApi.getWithdrawalInfo() }, withdrawalInfoLivedata, true)
    }

    fun withdrawal(type:Int?,withdrawalId:Int){
        request({ rxApi.withdrawal(type,withdrawalId)},withdrawalResultLivedata,
            isShowDialog = true,
            loadingHasContent = true
        )
    }
    fun roseExchange(id:Int){
        request({ rxApi.roseExchange(id)},exchangeResultLivedata,
            isShowDialog = true,
            loadingHasContent = true
        )
    }

    fun getWalletRecord(filterId:String,type: Int,page:Int){
        request({ rxApi.getWalletRecord(filterId,type,page)},walletRecordLivedata,true)
    }
}
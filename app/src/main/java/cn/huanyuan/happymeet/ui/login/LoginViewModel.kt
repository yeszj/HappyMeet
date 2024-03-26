package cn.huanyuan.happymeet.ui.login

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class LoginViewModel : BaseViewModel() {
    var phoneExt = ObservableField<String>()
    var codeExt = ObservableField<String>()

    var personInfo: MutableLiveData<BaseUserInfo> = MutableLiveData()
    var loginSuccessInfo: MutableLiveData<LoginSuccessInfo> = MutableLiveData()


    var loginLivedata: MutableLiveData<ResultState<LoginSuccessInfo>> = MutableLiveData()
    val codeLivedata = MutableLiveData<ResultState<String>>()
    val inviteCodeLivedata = MutableLiveData<ResultState<Boolean>>()
    val saveBasicInfoLivedata = MutableLiveData<ResultState<Boolean>>()

    fun sendVerifyCode() {
        request({ rxApi.sendVerifyCode(phoneExt.get().toString()) }, codeLivedata, false)
    }

    fun login(source: Int) {
        request(
            { rxApi.login(phoneExt.get().toString(), source, codeExt.get().toString()) },
            loginLivedata,
            true,
            loadingHasContent = true
        )
    }

    fun bindInviteCode(inviteCode: String) {
        request({ rxApi.bindInviteCode(inviteCode) }, inviteCodeLivedata, false)
    }

    fun insertBasicInfo() {
        val value = personInfo.value
        request(
            { rxApi.insertBasicInfo(value?.nickName, value!!.gender, value.age, value.province,value.city) },
            saveBasicInfoLivedata,
            true,
            loadingHasContent = true
        )
    }


}
package cn.huanyuan.sweetlove.ui.login

import android.text.TextUtils
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.net.rxApi
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
    var inviteCode = ObservableField<String>()

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
        val phone = phoneExt.get().toString()
        request(
            { rxApi.login(phone, source, codeExt.get().toString()) },
            loginLivedata,
            true,
            loadingHasContent = true
        )
    }

    fun jiGuangLogin(phone:String) {
        request(
            { rxApi.login(phone, 1, codeExt.get().toString()) },
            loginLivedata,
            true,
            loadingHasContent = true
        )
    }

    val getPhoneLivedata = MutableLiveData<ResultState<String>>()
    fun bindInviteCode(inviteCode: String,isEdit:Boolean = false) {
        if (isEdit){
            request({ rxApi.editInvite(inviteCode) }, inviteCodeLivedata, false)
        }else{
            request({ rxApi.bindInviteCode(inviteCode) }, inviteCodeLivedata, false)
        }
    }

    fun getPhone(loginToken: String) {
        request({ rxApi.getPhone(loginToken) }, getPhoneLivedata, false)
    }


    fun insertBasicInfo() {
        val value = personInfo.value
        request(
            {
                rxApi.insertBasicInfo(
                    value?.portrait,
                    value?.nickName,
                    value!!.gender,
                    value.age,
                    value.province,
                    value.city,
                    if(TextUtils.isEmpty(inviteCode.get()))"" else inviteCode.get().toString()
                )
            },
            saveBasicInfoLivedata,
            true,
            loadingHasContent = true
        )
    }


}
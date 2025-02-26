package cn.huanyuan.sweetlove.ui.userinfo.auth

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityRealNameBinding
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.RequestCodeManager
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.RegexUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.bean.CheckFaceAuthResult
import com.pcl.sdklib.bean.FaceAuthInfo
import com.pcl.sdklib.bean.PostBaiduAuthBean
import com.pcl.sdklib.sdk.faceAuth.FaceAuthActivity

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
@Route(path = RouterPath.ROUTER_REAL_NAME)
class RealNameActivity : BaseActivity<ActivityRealNameBinding, UserViewModel>(
    R.layout.activity_real_name,
    UserViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        val checkBaiduFaceResult =
            intent.getSerializableExtra(IntentKeyConfig.DATA) as CheckFaceAuthResult?
        if (checkBaiduFaceResult != null) {
            toCheckFace(checkBaiduFaceResult)
        } else {
            checkBaiduStep()
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnAuth.setOnSingleClickListener {
            if (checkValue()) {
                mViewModel.realNameProve(realName, idCard)
            }
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.FACE_RESULT).observe(this) {
            if (it){
                authSuccess()
            }else{
                authFail()
            }
        }
        LiveEventBus.get<String>(LiveDataEventManager.START_FACE_AUTH).observe(this){
            checkBaiduStep()
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.realNameObservable.observe(this) { it ->
            parseState(it, {
                faceAuthInfo = it
                checkIsCanFace()
            })
        }
        mViewModel.checkFaceResultObservable.observe(this) { it ->
            parseState(it, {
                toCheckFace(it)
            },{
                DialogUtils.dismissLoading()
                finish()
            })
        }
        mViewModel.canBaidFaceObservable.observe(this) { it ->
            parseState(it, {
                startBaiduFaceAuth()
            }, {
                if (it.code == ErrorCode.CODE_CANT_FACE_AUTH) {
                    DialogUtils.showConfirmDialog("温馨提示", {
                        ApplicationProxy.instance.askCustomer()
                    }, {
                    }, it.msg!!, cancel = "稍后再试", confirm = "联系客服")
                } else {
                    showToast(it.msg)
                }
                DialogUtils.dismissLoading()
            })
        }

        mViewModel.postBaidFaceObservable.observe(this@RealNameActivity) {
            parseState(it, {
                authSuccess()
            }, {
                authFail()

            })
        }
    }

    private fun checkBaiduStep() {
        DialogUtils.showLoading()
        mViewModel.checkBaiduStep()
    }


    private var hasRealName = false
    private var faceAuthInfo: FaceAuthInfo? = null
    private fun toCheckFace(it: CheckFaceAuthResult) {
        if (it.authId <= 2) {
            val params = it.params
            if (!TextUtils.isEmpty(params)) {
                hasRealName = true
                faceAuthInfo = GsonUtils.fromJson(params, FaceAuthInfo::class.java)
                realName = faceAuthInfo!!.realName
                if (!TextUtils.isEmpty(realName)) {
                    logcom("1.sessionId="+faceAuthInfo!!.sessionId)
                    checkIsCanFace()
                } else {
                    DialogUtils.dismissLoading()
                    mBinding.viewBg2.visibility = View.INVISIBLE
                }
            } else {
                DialogUtils.dismissLoading()
                mBinding.viewBg2.visibility = View.INVISIBLE
            }
        } else {
            DialogUtils.dismissLoading()
            mBinding.viewBg2.visibility = View.INVISIBLE
        }
    }


    private fun checkIsCanFace() {
        DialogUtils.showLoading()
        mViewModel.ifCanBaiduFace()
    }

    private fun startBaiduFaceAuth() {
        DialogUtils.dismissLoading()
        faceAuthInfo?.apply {
            logcom("2.sessionId="+this.sessionId)
            FaceAuthActivity.lunch(mContext,this)
        }
//        val baiduPackBean = BaiduPackBean(realName, idCard, access_token)
//        BaiduFaceAuthUtils.getInstance().startBaiduFaceAuth(
//            mContext,
//            baiduPackBean,
//            object : BaiduFaceAuthUtils.OnSubmitAuthListener {
//                override fun onAuthSuccess(idCardImage: String, score: String) {
//                    submitBaiduFace(idCardImage, score)
//                }
//
//                override fun onAuthFail() {
//                    authFail()
//                }
//            })
    }

    private fun submitBaiduFace(idCardImage: String, score: String) {
        val baiduFaceResult = PostBaiduAuthBean(
            idCardImage,
            score,
            realName,
            idCard
        )
        mViewModel.submitBaiduFace(baiduFaceResult)

    }

    private fun authSuccess() {
        showToast("认证成功")
        DialogUtils.dismissLoading()
        setResult(RESULT_OK)
        finish()
    }

    private fun authFail() {
        DialogUtils.dismissLoading()
        if (hasRealName) {
            finish()
        }
    }

    private var realName: String = ""
    private var idCard: String = ""
    private fun checkValue(): Boolean {
        realName = mBinding.etRealName.text.toString().trim()
        if (TextUtils.isEmpty(realName)) {
            showToast("请填写真实姓名")
            return false
        }
        idCard = mBinding.etIdCard.text.toString().trim()
        if (!RegexUtils.isIDCard18(idCard)) {
            showToast("请填写正确的身份证")
            return false
        }
        return true
    }

    companion object {
        fun lunch(context: Activity, checkBaiduFaceResult: CheckFaceAuthResult? = null) {
            val intent = Intent(context, RealNameActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, checkBaiduFaceResult)
            context.startActivityForResult(
                intent,
                RequestCodeManager.REQUEST_CODE_REAL_NAME
            )
        }
    }
}
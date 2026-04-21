package cn.huanyuan.sweetlove.ui.userinfo.auth

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityRealNameBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.RequestCodeManager
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.RegexUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.bean.CheckFaceAuthResult
import com.pcl.sdklib.bean.FaceAuthInfo
import com.pcl.sdklib.sdk.faceAuth.FaceAuthActivity

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
@Route(path = RouterPath.ROUTER_REAL_NAME)
class RealNameActivity : BaseActivity<ActivityRealNameBinding, UserViewModel>(
    R.layout.activity_real_name, UserViewModel::class.java
) {
    private var source: Int = 1
    private var isConsumeGold: Boolean = false
    private var checkBaiduFaceResult: CheckFaceAuthResult? = null
    private var isRefaceAuth = false
    override fun initData() {
        source = intent.getIntExtra(IntentKeyConfig.SOURCE, 1)
        isConsumeGold = intent.getBooleanExtra("isConsumeGold", false)
        isRefaceAuth = intent.getBooleanExtra("isRefaceAuth", false)

        setStatusBarStyle(false)
        checkBaiduFaceResult =
            intent.getSerializableExtra(IntentKeyConfig.DATA) as CheckFaceAuthResult?
        if (checkBaiduFaceResult != null) {
            toCheckFace(checkBaiduFaceResult!!)
        } else {
            if (isRefaceAuth) {
                getFaceAuthInfo()
            } else {
                checkBaiduStep()
            }
        }
    }

    private fun getFaceAuthInfo() {
        DialogUtils.showLoading()
        request(
            { rxApi.getFaceAuthInfo() }, object : OnRequestResultListener<FaceAuthInfo> {
                override fun onSuccess(data: BaseBean<FaceAuthInfo>) {
                    DialogUtils.dismissLoading()
                    val faceInfo = data.data ?: return
                    checkBaiduFaceResult = CheckFaceAuthResult(2, GsonUtils.toJson(faceInfo), false)
                    toCheckFace(checkBaiduFaceResult!!)
                }

                override fun onFail(code: Int?, msg: String?) {
                    DialogUtils.dismissLoading()
                    finish()
                }
            }, true
        )
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnAuth.setOnSingleClickListener {
            if (checkValue()) {
                DialogUtils.showLoading()
                mViewModel.realNameAuthPreCheck(realName, idCard)
            }
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.FACE_RESULT).observe(this) {
            if (it) {
                authSuccess()
            } else {
                authFail()
            }
        }
        LiveEventBus.get<String>(LiveDataEventManager.START_FACE_AUTH).observe(this) {
            if (checkBaiduFaceResult != null && checkBaiduFaceResult?.isChangeDevice == true) {
                toCheckFaceDeviceChange()
            } else {
                checkBaiduStep()
            }
        }
    }

    private fun toCheckFaceDeviceChange() {
        request({ rxApi.getFaceAuthInfo() }, object : OnRequestResultListener<FaceAuthInfo> {
            override fun onSuccess(data: BaseBean<FaceAuthInfo>) {
                val faceInfo = data.data ?: return
                checkBaiduFaceResult = CheckFaceAuthResult(2, GsonUtils.toJson(faceInfo), true)
                toCheckFace(checkBaiduFaceResult!!)
            }

            override fun onFail(code: Int?, msg: String?) {
            }

        }, true)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.realNamePreCheckObservable.observe(this) {
            parseState(it, {
                if (it.needConfirm) {
                    showConfirmPop()
                    DialogUtils.dismissLoading()
                } else {
                    faceAuthInfo = it
                    checkIsCanFace()
                }
            }, {
                DialogUtils.dismissLoading()
            })
        }
        mViewModel.realNameObservable.observe(this) { it ->
            parseState(it, {
                DialogUtils.dismissLoading()
                faceAuthInfo = it
                checkIsCanFace()
            }, {
                DialogUtils.dismissLoading()
            })
        }
        mViewModel.checkFaceResultObservable.observe(this) { it ->
            parseState(it, {
                toCheckFace(it)
            }, {
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
                        finish()
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

    private fun showConfirmPop() {
        val defaultColor = CommonUtils.getColor(cn.yanhu.commonres.R.color.color_666666)
        val redColor = CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain)
        val build = Spans.builder().text("该身份信息").color(defaultColor).text("已绑定上级账号")
            .color(redColor).text("，继续实名").color(defaultColor).text("将清除现有邀请关系")
            .color(redColor).text("，是否仍要使用？").color(defaultColor).build()
        DialogUtils.showConfirmDialog("实名认证确认", {
            mViewModel.realNameProve(realName, idCard)
        }, {
            mBinding.etRealName.setText("")
            mBinding.etIdCard.setText("")
        }, build, cancel = "更换信息", confirm = "继续实名")
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
                    logcom("1.sessionId=" + faceAuthInfo!!.sessionId)
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
            logcom("2.sessionId=" + this.sessionId)
            FaceAuthActivity.lunch(mContext, this, source, isConsumeGold)
        }
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
        fun lunch(
            context: Activity,
            checkBaiduFaceResult: CheckFaceAuthResult? = null,
            source: Int = 1,
            isConsumeGold: Boolean = false
        ) {
            val intent = Intent(context, RealNameActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, checkBaiduFaceResult)
            intent.putExtra(IntentKeyConfig.SOURCE, source)
            intent.putExtra("isConsumeGold", isConsumeGold)
            context.startActivityForResult(
                intent, RequestCodeManager.REQUEST_CODE_REAL_NAME
            )
        }
    }
}
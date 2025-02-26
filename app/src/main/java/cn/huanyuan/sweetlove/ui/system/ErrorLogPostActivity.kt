package cn.huanyuan.sweetlove.ui.system

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.huanyuan.sweetlove.BuildConfig
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.ErrorLogInfo
import cn.huanyuan.sweetlove.databinding.ActivityErrorLogPostBinding
import cn.huanyuan.sweetlove.func.dialog.ErrorLogTimeSelectPop
import cn.huanyuan.sweetlove.ui.system.adapter.ErrorTypeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.StringUtils
import com.pcl.sdklib.sdk.wechat.WxCustomerServiceUtils

/**
 * @author: zhengjun
 * created: 2024/3/13
 * desc:日志上报
 */
@Route(path = RouterPath.ROUTER_ERRORLOGPOST)
class ErrorLogPostActivity : BaseActivity<ActivityErrorLogPostBinding, SystemViewModel>(
    R.layout.activity_error_log_post,
    SystemViewModel::class.java
) {

    private val complaintTypeAdapter by lazy { ErrorTypeAdapter() }
    private var selectPosition: Int = 0
    private var extraInfo: String = ""
    override fun initData() {
        selectPosition = intent.getIntExtra(IntentKeyConfig.POSITION, 0)
        extraInfo = intent.getStringExtra(IntentKeyConfig.DATA).toString()
        setStatusBarStyle(false)
        mBinding.rvType.adapter = complaintTypeAdapter
        val stringArray =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.error_type)
        complaintTypeAdapter.submitList(stringArray.toMutableList())
        complaintTypeAdapter.setSelectPosition(selectPosition)
        complaintTypeAdapter.setOnItemClickListener { _, _, position ->
            val item = complaintTypeAdapter.getItem(position)
            mViewModel.complaintInfo.value?.typeIds = item!!
            complaintTypeAdapter.setSelectPosition(position)
        }
        val build = Spans.builder().text(
            "上传日志可以帮助我们更好的定位和解决问题。\n" +
                    "此功能请在客服指导下使用。"
        ).text("联系客服").click(
            mBinding.tvTips,
            CustomClickSpan(
                mContext,
                CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue),
                object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        WxCustomerServiceUtils.askCustomer()
                    }
                })
        ).build()
        mBinding.tvTips.text = build
    }

    private var logInfo: ErrorLogInfo = ErrorLogInfo()
    override fun initListener() {
        super.initListener()
        mBinding.tvTime.setOnSingleClickListener {
            ErrorLogTimeSelectPop.showDialog(mContext,
                object : ErrorLogTimeSelectPop.OnSelectTimeListener {
                    override fun onSelect(time: String) {
                        mBinding.tvTime.text = time
                    }
                })
        }
        mBinding.btnCommit.setOnSingleClickListener {
            val questionType = complaintTypeAdapter.getSelectItem()
            if (TextUtils.isEmpty(questionType)) {
                showToast("请选择问题类型")
            } else {
                val timeValue = mBinding.tvTime.text.toString()
                if (TextUtils.isEmpty(timeValue)) {
                    showToast("请选择问题发生时间")
                } else {
                    val reason = mBinding.etReason.text.toString()
                    logInfo.typeDesc = questionType!!
                    logInfo.errorTime = timeValue
                    logInfo.description = reason
                    logInfo.extInfo = extraInfo
                    uploadErrorFile()
                }
            }
        }
    }

    private fun uploadErrorFile() {
        DialogUtils.showLoading("正在上传...")
        val logPath =
            "/storage/emulated/0/Android/data/" + BuildConfig.APPLICATION_ID + "/files/agorasdk.log"
        mViewModel.uploadFile(logPath,2,object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                uploadLog(data.data)
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })
    }

    private fun uploadLog(url: String?) {
        if (TextUtils.isEmpty(url)){
            DialogUtils.dismissLoading()
            return
        }
        logInfo.url = url!!
        mViewModel.uploadLog(logInfo, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                DialogUtils.dismissLoading()
                showToast("上传日志成功")
                finish()
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, ErrorLogPostActivity::class.java))
        }
    }
}
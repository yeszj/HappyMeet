package cn.huanyuan.sweetlove.ui.system

import android.content.Context
import android.content.Intent
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityErrorLogPostBinding
import cn.huanyuan.sweetlove.func.dialog.ErrorLogTimeSelectPop
import cn.huanyuan.sweetlove.ui.system.adapter.ErrorTypeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import com.blankj.utilcode.util.StringUtils
import com.pcl.sdklib.sdk.wechat.WxCustomerServiceUtils

/**
 * @author: zhengjun
 * created: 2024/3/13
 * desc:日志上报
 */
class ErrorLogPostActivity : BaseActivity<ActivityErrorLogPostBinding, SystemViewModel>(
    R.layout.activity_error_log_post,
    SystemViewModel::class.java
) {

    private val complaintTypeAdapter by lazy { ErrorTypeAdapter() }

    override fun initData() {
        setStatusBarStyle(false)
        mBinding.rvType.adapter = complaintTypeAdapter
        val stringArray =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.error_type)
        complaintTypeAdapter.submitList(stringArray.toMutableList())
        complaintTypeAdapter.setSelectPosition(0)
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
    }

    companion object {
        fun lunch(context: Context) {
            context.startActivity(Intent(context, ErrorLogPostActivity::class.java))
        }
    }
}
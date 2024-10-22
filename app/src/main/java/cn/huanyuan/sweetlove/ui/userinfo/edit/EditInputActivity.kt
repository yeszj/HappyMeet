package cn.huanyuan.sweetlove.ui.userinfo.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.InputFilter
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityEditInputBinding
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.RequestCodeManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.KeyboardUtils
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class EditInputActivity : BaseActivity<ActivityEditInputBinding, UserViewModel>(
    R.layout.activity_edit_input,
    UserViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        initContent()
        mBinding.etContent.requestFocus()
        mBinding.etContent.isFocusableInTouchMode = true
        mBinding.etContent.isFocusable = true
        KeyboardUtils.showSoftInput()
    }

    override fun initListener() {
        initTitleListener()
    }

    private fun initTitleListener() {
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            @SuppressLint("UnsafeIntentLaunch")
            override fun rightButtonOnClick(v: View?) {
                val inputValue = mBinding.etContent.text.toString().trim()
                if (TextUtils.isEmpty(inputValue)){
                    showToast("内容不能为空")
                    mBinding.etContent.setShakeAnimation()
                    return
                }
                if (type== TYPE_INPUT_NAME){
                    saveEditInfo(4,inputValue)
                }else{
                    saveEditInfo(3,inputValue)
                }

            }
        })
    }

    fun saveEditInfo(type:Int,content:String){
        mViewModel.updatePersonalPageSingle(type,content,object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                intent.putExtra(IntentKeyConfig.DATA, content)
                setResult(RESULT_OK, intent)
                finish()
                LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_USER_INFO).post(true)
            }
        })
    }

    private var type:Int = TYPE_INPUT_NAME
    private fun initContent() {
         type = intent.getIntExtra(IntentKeyConfig.TYPE, TYPE_INPUT_NAME)
        val content = intent.getStringExtra(IntentKeyConfig.DATA).toString()
        when (type) {
            TYPE_INPUT_NAME -> {
                mBinding.titleBar.setLeftTitleName("昵称")
                mBinding.etContent.apply {
                    minHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_48)
                    hint = "请输入昵称"
                    maxLines = 1
                    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
                    gravity = Gravity.CENTER_VERTICAL

                }
            }

            TYPE_INPUT_DESC -> {
                mBinding.titleBar.setLeftTitleName("交友心声")
                mBinding.etContent.apply {
                    minHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
                    hint = "说说你的交友心声..."
                    maxLines = 20
                    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(140))
                    gravity = Gravity.TOP
                }
            }
        }
        mBinding.etContent.apply {
            setText(content)
            setSelection(content.length)
        }
    }


    override fun exactDestroy() {
        super.exactDestroy()
        KeyboardUtils.hideSoftInput(mContext)
    }

    companion object {
        const val TYPE_INPUT_NAME = 1
        const val TYPE_INPUT_DESC = 2

        fun lunch(context: Activity, type: Int, content: String?) {
            val intent = Intent(context, EditInputActivity::class.java)
            intent.putExtra(IntentKeyConfig.TYPE, type)
            intent.putExtra(IntentKeyConfig.DATA, content)
            context.startActivityForResult(intent, RequestCodeManager.REQUEST_CODE_INPUT_CONTENT)
        }
    }
}
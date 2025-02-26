package com.pcl.sdklib.sdk.faceAuth

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import com.pcl.sdklib.R
import com.pcl.sdklib.databinding.ActivityFaceAuthResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel

/**
 * @author: zhengjun
 * created: 2025/2/20
 * desc:
 */
class FaceAuthResultActivity : BaseActivity<ActivityFaceAuthResultBinding, FaceAuthViewModel>(
    R.layout.activity_face_auth_result,
    FaceAuthViewModel::class.java
) {

    companion object {
        fun lunch(context: Context, result: String?) {
            val intent = Intent(context, FaceAuthResultActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, result)
            context.startActivity(intent)
        }
    }

    private var isSuccess = false
    override fun initData() {
        setStatusBarStyle(false)
        val result = intent.getStringExtra(IntentKeyConfig.DATA)
        if ("success" == result) {
            isSuccess = true
            mBinding.icon.setImageResource(R.drawable.ic_round_check_circle_24)
            mBinding.title.setText(R.string.label_detect_success)
            mBinding.btnReturnHome.visibility = View.INVISIBLE
        } else {
            mBinding.icon.setImageResource(R.drawable.ic_round_cancel_24)
            mBinding.title.text = result
            mBinding.tvSure.text = "重新采集"
            mBinding.btnReturnHome.visibility = View.VISIBLE
        }
        mBinding.tvSure.setOnSingleClickListener {
            if (isSuccess){
                finishResult()
            }else{
                LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.START_FACE_AUTH)
                finish()
            }
        }
        mBinding.btnReturnHome.setOnSingleClickListener {
            finishResult()
        }
        if (isSuccess){
            startCountTime()
        }
    }


    private fun finishResult() {
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.FACE_RESULT, isSuccess)
        finish()
    }

    private var countDown: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startCountTime() {
        countDown?.cancel()
        countDown(5, start = {
            countDown = it
        }, end = {
            //倒计时结束
            finishResult()
        }, next = {
        }, cancel = {})
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
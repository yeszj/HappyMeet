package com.pcl.sdklib.sdk.faceAuth

import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.MutableLiveData
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.bean.FaceParamsBody
import com.sensetime.senseid.Frame
import com.sensetime.senseid.LabelId
import com.sensetime.senseid.Message
import com.sensetime.senseid.api.SenseidApi
import com.sensetime.senseid.api.SenseidApiInterface
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author: zhengjun
 * created: 2025/2/20
 * desc:
 */
class FaceAuthViewModel : BaseViewModel() {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    var initialResult = MutableLiveData<Int>()

    var detectMessage = MutableLiveData<Pair<Int, Message>>()

    var completeMessage = MutableLiveData<Pair<Int, String>>()

    private var api: SenseidApi? = SenseidApi()

    private var sdkInitResult = 0

    fun checkFaceResult(sessionId:String,onRequestResultListener: OnRequestResultListener<String>) {
        val faceParamsBody = FaceParamsBody(sessionId)
        request({ sdkRxApi.checkFaceAuthResult(faceParamsBody) }, onRequestResultListener,false)
    }

    fun initialize(labelId: LabelId?, token: String?, uuid: String?, session: String?) {
        Log.d("TAG", "version: " + api!!.version)
        executor.execute {
//            api!!.onLogConfig(true) { s ->
//                logcom(" $s")
//            }
            val result = api!!.initialize(labelId, token, uuid, session, true)
            sdkInitResult = result
            api!!.setCallback(object : SenseidApiInterface.Callback {
                override fun onUpdate(code: Int, message: Message) {
                    detectMessage.postValue(
                        Pair(
                            code,
                            message
                        )
                    )
                }

                override fun onComplete(code: Int, message: String) {
                    completeMessage.postValue(
                        Pair(
                            code,
                            message
                        )
                    )
                }
            })
            if (0 == result) {
                api!!.start()
            }
            initialResult.postValue(result)
        }
    }

    fun input(frame: Frame?) {
        executor.execute { api!!.input(frame) }
    }

    fun release() {
        executor.execute {
            if (0 == sdkInitResult) {
                api!!.cancel()
                api!!.release()
            } else {
                api = null
            }
        }
    }
}
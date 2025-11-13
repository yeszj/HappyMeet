package cn.yanhu.imchat.manager

import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.commonres.bean.CommonErrorTipsInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.imchat.api.imChatRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2025/11/12
 * desc:
 */
object SendGiftCheckManager {
    val sendGiftRequest = SendGiftRequest()
    fun checkSendGift(toUid: String, giftId: Int, onCheckGiftListener: OnCheckGiftListener) {
        sendGiftRequest.toUid = toUid
        sendGiftRequest.giftId = giftId
        sendGiftRequest.num = 1
        request(
            { imChatRxApi.checkSendGift(sendGiftRequest) },
            object : OnRequestResultListener<CommonErrorTipsInfo> {
                override fun onSuccess(data: BaseBean<CommonErrorTipsInfo>) {
                    val errorTipInfo = data.data
                    if (errorTipInfo == null) {
                        onCheckGiftListener.onCanSend()
                    } else {
                        DialogUtils.showConfirmDialog(errorTipInfo.title, {
                            request2({imChatRxApi.clickConfirm(sendGiftRequest)},object : OnRequestResultListener<String>{
                                override fun onSuccess(data: BaseBean<String>) {
                                }
                            })
                            onCheckGiftListener.onCanSend()
                        }, {
                        }, errorTipInfo.content, cancel = "取消", confirm = "确认赠送")
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                }
            })
    }

    interface OnCheckGiftListener {
        fun onCanSend()
    }
}
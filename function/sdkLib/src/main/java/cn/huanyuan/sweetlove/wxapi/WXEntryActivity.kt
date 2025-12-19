package cn.huanyuan.sweetlove.wxapi

import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.sdk.wechat.WxAuthUtils
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.json.JSONException
import org.json.JSONObject

class WXEntryActivity : WXCallbackActivity() {
    override fun onResp(resp: BaseResp) {
        if (!WxAuthUtils.isWxAuth){
            super.onResp(resp)
        }
        if (resp.type == ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW) {
            val launchMiniProgramResp = resp as WXOpenBusinessView.Resp
            val extMsg = launchMiniProgramResp.extMsg
            try {
                val jsonObject = JSONObject(extMsg)
                val result = jsonObject.optString("result", "")
                when (result) {
                    "success" -> {
                        showToast("确认收款成功")
                        LiveEventBus.get<Boolean>(EventBusKeyConfig.REQUESTMERCHANTTRANSFERSUCCESS)
                            .post(true)
                    }

                    "fail" -> showToast("确认收款失败：" + resp.errCode)
                    "cancel" -> showToast("已取消")
                    else -> showToast(resp.errStr)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }else{
            when (resp.errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    if (resp is SendAuth.Resp) {
                        logcom("微信sdk回调授权成功")
                        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.WX_AUTH_SUCCESS,resp.code)
                    } else if (resp is SendMessageToWX.Resp) {
                        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.WX_SHARE_SUCCESS,true)
                        logcom("微信sdk回调分享成功")
                    }
                }

                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    logcom("取消授权")
                }

                BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                    logcom("拒绝授权")
                }

                else -> {

                }
            }
        }
        finish()
    }
}
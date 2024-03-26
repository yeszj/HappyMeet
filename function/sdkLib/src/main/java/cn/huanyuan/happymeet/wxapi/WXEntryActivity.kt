package cn.huanyuan.happymeet.wxapi

import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.LiveDataEventManager
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.umeng.socialize.weixin.view.WXCallbackActivity

class WXEntryActivity : WXCallbackActivity() {
    override fun onResp(resp: BaseResp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (resp is SendAuth.Resp) {
                    logcom("微信sdk回调授权成功")
                    LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.WX_AUTH_SUCCESS,resp.code)
                } else if (resp is SendMessageToWX.Resp) {
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
        finish()
    }
}
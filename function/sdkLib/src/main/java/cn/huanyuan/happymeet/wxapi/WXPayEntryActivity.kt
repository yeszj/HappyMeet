package cn.huanyuan.happymeet.wxapi

import android.content.Intent
import android.os.Bundle
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.LiveDataEventManager.sendLiveDataMessage
import com.pcl.sdklib.manager.SdkParamsManager
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.weixin.view.WXCallbackActivity

class WXPayEntryActivity : WXCallbackActivity() {
    private var api: IWXAPI? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, SdkParamsManager.WX_APP_ID, false)
        try {
            api?.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {}
    override fun onResp(resp: BaseResp) {
        //有时候支付结果还需要发送给服务器确认支付状态
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            when (resp.errCode) {
                0 -> {
                    showToast("支付成功")
                    sendLiveDataMessage(LiveDataEventManager.PAY_RESULT, true)
                }
                -2 -> {
                    showToast("取消支付")
                }
                else -> {
                    showToast("支付失败")
                    sendLiveDataMessage(LiveDataEventManager.PAY_RESULT, false)
                }
            }
            finish()
        }
    }
}
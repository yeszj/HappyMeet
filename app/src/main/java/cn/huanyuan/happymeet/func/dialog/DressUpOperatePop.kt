package cn.huanyuan.happymeet.func.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.PopDressUpOperateBinding
import cn.huanyuan.happymeet.net.rxApi
import cn.huanyuan.happymeet.ui.userinfo.dressUp.DressUpFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.DressUpInfo
import cn.yanhu.commonres.bean.request.DressUpRequest
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.request
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
@SuppressLint("ViewConstructor")
class DressUpOperatePop(
    val context: FragmentActivity,
    val item: DressUpInfo,
    val type: Int,
    private val roseBalance: String
) :
    BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_dress_up_operate
    }

    private var mBinding: PopDressUpOperateBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDressUpOperateBinding.bind(popupImplView)
        mBinding?.apply {
            if (type == DressUpFrg.TYPE_CAR) {
                ViewUtils.setViewSize(
                    ivCover,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_196),
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_196)
                )
            }
            itemInfo = item
            if (TextUtils.isEmpty(item.svgUrl)) {
                GlideUtils.load(context, item.cover, ivCover)
            } else {
                SVGAUtils.loadSVGAAnim(ivCover, item.svgUrl)
            }
            tvBuy.setOnSingleClickListener {
                if (item.isTimeOut){
                    DressGoodsBuyPop.showDialog(context,item,type,roseBalance)
                    dismiss()
                }else {
                    startWear(item.isWear)
                }
            }
        }
    }


    private fun startWear(isWear:Boolean) {
        val buyRequest = DressUpRequest(
            item.id,
            type,
            if (isWear) 0 else 1,
        )
        request({ rxApi.dressUpCommodity(buyRequest) },object : OnBooleanResultListener{
            override fun onSuccess() {
                if (isWear){
                    showToast("取消佩戴成功")
                }else{
                    showToast("佩戴成功")
                }
                item.isWear = !isWear
                mBinding?.itemInfo = item
                LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.DRESS_UP_SUCCESS,item.id)
            }
        })
    }



    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            item: DressUpInfo,
            type: Int,
            roseBalance: String
        ): DressUpOperatePop {
            val matchPop = DressUpOperatePop(mContext, item, type, roseBalance)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }
}
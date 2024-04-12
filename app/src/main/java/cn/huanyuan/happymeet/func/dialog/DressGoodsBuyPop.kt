package cn.huanyuan.happymeet.func.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.PopDressGoodsBuyBinding
import cn.huanyuan.happymeet.net.rxApi
import cn.huanyuan.happymeet.ui.userinfo.dressUp.DressUpFrg
import cn.huanyuan.happymeet.ui.userinfo.dressUp.adapter.GoodsPriceAdapter
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.request.DressBuyRequest
import cn.yanhu.commonres.bean.DressUpInfo
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
@SuppressLint("ViewConstructor")
class DressGoodsBuyPop(
    val mContext: FragmentActivity,
    val item: DressUpInfo,
    val type: Int,
    private val roseBalance: String
) :
    BottomPopupView(mContext) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_dress_goods_buy
    }

    private val priceAdapter by lazy { GoodsPriceAdapter() }
    private var mBinding: PopDressGoodsBuyBinding? = null
    private var selectUserInfo: BaseUserInfo? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDressGoodsBuyBinding.bind(popupImplView)
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
            recyclerView.adapter = priceAdapter
            priceAdapter.submitList(item.priceList)
            priceAdapter.setOnItemClickListener { _, _, position ->
                priceAdapter.setSelectPosition(
                    position
                )
            }
            tvGive.setOnSingleClickListener {
                toSelectFriend()
            }
            tvBuy.setOnSingleClickListener {
                startBuy()
            }
        }
    }

    private fun toSelectFriend() {
        if (checkBalance()) {

        }
    }

    private fun startBuy() {
        if (checkBalance()) {
            val selectItem = priceAdapter.getSelectItem()
            val buyRequest = DressBuyRequest(
                item.id,
                type,
                selectItem!!.id,
                if (selectUserInfo != null) selectUserInfo?.userId else ""
            )
            request({ rxApi.userBuyCommodity(buyRequest) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        showToast("购买成功")
                        if (TextUtils.isEmpty(buyRequest.friendUserId)){
                            //如果不是赠送好友 通知列表更新数据-显示已拥有标签
                            LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.DRESS_BUY_SUCCESS,buyRequest.commodityId)
                        }
                        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.ROSE_BALANCE_CHANGE,data.data!!)
                        dismiss()
                    }
                })
        }
    }

    private fun checkBalance(): Boolean {
        val selectItem = priceAdapter.getSelectItem()
        selectItem?.apply {
            if (!CommonUtils.compareString(roseBalance, selectItem.price)) {
                showToast("余额不足，请先充值")
                showRechargeDialog()
                return false
            }
        }
        return true
    }

    private var roseRechargePop:RoseRechargePop?=null
    private fun showRechargeDialog() {
        if (CommonUtils.isPopShow(roseRechargePop)){
            return
        }
        roseRechargePop = RoseRechargePop.showDialog(mContext)
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            item: DressUpInfo,
            type: Int,
            roseBalance: String
        ): DressGoodsBuyPop {
            val matchPop = DressGoodsBuyPop(mContext, item, type, roseBalance)
            val builder = XPopup.Builder(mContext)
            builder
                .asCustom(matchPop).show()
            return matchPop
        }
    }
}
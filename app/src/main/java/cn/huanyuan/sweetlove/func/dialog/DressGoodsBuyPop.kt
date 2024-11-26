package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopDressGoodsBuyBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.userinfo.dressUp.DressUpFrg
import cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter.GoodsPriceAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.pop.FriendsSelectPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.request.DressBuyRequest
import cn.yanhu.commonres.bean.DressUpInfo
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.bean.response.FriendsResponse
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.opensource.svgaplayer.SVGACallback

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

    private  var friendList: MutableList<SameCityUserInfo> = mutableListOf()
    private val priceAdapter by lazy { GoodsPriceAdapter() }
    private var mBinding: PopDressGoodsBuyBinding? = null
    private var selectUserInfo: BaseUserInfo? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDressGoodsBuyBinding.bind(popupImplView)
        mBinding?.apply {
            itemInfo = item
            val svga = item.svgUrl

            if (!TextUtils.isEmpty(svga)) {
                if (type != DressUpFrg.TYPE_CAR) {
                    if (type == DressUpFrg.TYPE_USER_FLOAT) {
                        ViewUtils.setViewSize(
                            ivGoods,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_187),
                            CommonUtils.getDimension(
                                com.zj.dimens.R.dimen.dp_50
                            )
                        )
                    }
                    tvRead.visibility = View.INVISIBLE
                    SVGAUtils.loadSVGAAnim(ivGoods, svga)
                } else {
                    tvRead.visibility = View.VISIBLE
                    vgPic.setOnSingleClickListener {
                        SVGAUtils.loadSVGAAnim(svgaImageView, svga)
                    }
                    // 设置回调
                    svgaImageView.callback = object : SVGACallback {
                        override fun onPause() {
                        }

                        override fun onFinished() {
                            svgaImageView.clear()
                        }

                        override fun onRepeat() {}
                        override fun onStep(i: Int, v: Double) {}
                    }
                }
            } else {
                tvRead.visibility = View.INVISIBLE
            }
            loadDrawable()
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
                selectUserInfo = null
                startBuy()
            }
            vgOutline.setOnSingleClickListener { dismiss() }
            vgContent.setOnSingleClickListener { }
            getFriendList()
        }
    }

    private fun loadDrawable() {
        GlideUtils.loadAsDrawable(context, item.cover, object :
            CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                mBinding!!.ivGoods.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })
    }

    private var friendsSelectPop:FriendsSelectPop?=null
    private fun toSelectFriend() {
        if(CommonUtils.isPopShow(friendsSelectPop)){
            return
        }
        if (checkBalance()) {
            friendsSelectPop = FriendsSelectPop.showDialog(context,friendList,object : FriendsSelectPop.OnSelectFriendListener{
                override fun onSelectUser(userInfo: SameCityUserInfo) {
                    selectUserInfo = userInfo
                    val content = Spans.builder()
                        .text( "确定赠送${item.name}给${userInfo.nickName}吗？\n\n")
                        .text("温馨提示：赠送需花费${item.showPrice}玫瑰")
                        .color(
                            CommonUtils.getColor(
                                cn.yanhu.baselib.R.color.colorMain
                            )
                        ).build()
                    DialogUtils.showConfirmDialog("赠送提示",{
                        startBuy()
                    },{
                        selectUserInfo = null
                    },content)
                }
            })
        }
    }

    private fun getFriendList(){
        request({ agoraRxApi.getFriendList(1)},object : OnRequestResultListener<FriendsResponse>{
            override fun onSuccess(data: BaseBean<FriendsResponse>) {
                val friendsResponse = data.data?:return
                 friendList = friendsResponse.list

            }
        })
    }

    private fun startBuy() {
        if (checkBalance()) {
            val selectItem = priceAdapter.getSelectItem()
            val buyRequest = DressBuyRequest(
                item.id,
                selectItem!!.id,
                if (selectUserInfo != null) selectUserInfo?.userId else ""
            )
            request({ rxApi.userBuyCommodity(buyRequest) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        if (TextUtils.isEmpty(buyRequest.friendUserId)) {
                            showToast("购买成功")
                            LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_USER_INFO)
                                .post(true)
                            //如果不是赠送好友 通知列表更新数据-显示已拥有标签
                            LiveDataEventManager.sendLiveDataMessage(
                                LiveDataEventManager.DRESS_BUY_SUCCESS,
                                buyRequest.commodityId
                            )
                        }else{
                            showToast("赠送成功")
                        }
                        LiveDataEventManager.sendLiveDataMessage(
                            LiveDataEventManager.ROSE_BALANCE_CHANGE,
                            data.data!!
                        )
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

    private var roseRechargePop: RoseRechargePop? = null
    private fun showRechargeDialog() {
        if (CommonUtils.isPopShow(roseRechargePop)) {
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
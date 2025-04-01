package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.RoomWishAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.request.CreateWishRequest
import cn.yanhu.commonres.bean.response.WishResponse
import cn.yanhu.agora.databinding.PopAddWishBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.WishInfo
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/3/26
 * desc:
 */
@SuppressLint("ViewConstructor")
class RoomWishListPop(
    context: Context,
    var roomId: String,
    private var wishResponse: WishResponse,
    val isOwner: Boolean,
    val onSendGiftListener: OnSendGiftListener
) : BottomPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            roomId: String,
            wishResponse: WishResponse,
            isOwner: Boolean,
            onSendGiftListener: OnSendGiftListener
        ): RoomWishListPop {
            val matchPop =
                RoomWishListPop(mContext, roomId, wishResponse, isOwner, onSendGiftListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_add_wish
    }

    private lateinit var mBinding: PopAddWishBinding
    private val roomWishAdapter by lazy {
        RoomWishAdapter(isOwner)
    }

    override fun onCreate() {
        super.onCreate()
        mBinding = PopAddWishBinding.bind(popupImplView)
        initWishListAdapter()

        setBtnStatus()

    }

    private fun setBtnStatus() {
        if (isOwner) {
            mBinding.tvTips.text = CommonUtils.getString(R.string.wish_desc)
            mBinding.ivWish.visibility = VISIBLE
            if (wishResponse.status == 0) {
                mBinding.tvTitle.text = "生成心愿单"
                mBinding.ivWish.setImageResource(R.drawable.btn_add_wish)
            } else {
                mBinding.tvTitle.text = "修改心愿单"
                mBinding.ivWish.setImageResource(R.drawable.btn_edit_wish)
            }
            mBinding.ivWish.setOnSingleClickListener {
                dismiss()
            }
        } else {
            mBinding.tvTips.text = "快帮主持完成心愿吧~"
            mBinding.tvTitle.text = "主播今日心愿单"
            mBinding.ivWish.visibility = GONE
        }
    }

    fun refreshWishList(wishResponse: WishResponse) {
        this.wishResponse = wishResponse
        val list = wishResponse.list
        val layoutManager = mBinding.rvWish.layoutManager as GridLayoutManager
        layoutManager.spanCount = list.size
        mBinding.rvWish.layoutManager = layoutManager
        roomWishAdapter.submitList(list)
    }


    private fun initWishListAdapter() {
        val list = wishResponse.list
        val layoutManager = GridLayoutManager(context, list.size)
        mBinding.rvWish.layoutManager = layoutManager
        mBinding.rvWish.adapter = roomWishAdapter
        roomWishAdapter.submitList(list)
        roomWishAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<WishInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<WishInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position) ?: return
                if (isOwner && item.sunNum == 0) {
                    showSelectGift(position)
                }
            }
        })
        roomWishAdapter.addOnDebouncedChildClick(
            R.id.iv_send,
            500,
            object : BaseQuickAdapter.OnItemChildClickListener<WishInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<WishInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = adapter.getItem(position) ?: return
                    onSendGiftListener.onSendGift(item)
                }
            })
        roomWishAdapter.addOnDebouncedChildClick(
            R.id.iv_delete,
            500,
            object : BaseQuickAdapter.OnItemChildClickListener<WishInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<WishInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = adapter.getItem(position) ?: return
                    item.sunNum = 0
                    item.id = 0
                    roomWishAdapter.notifyItemChanged(position)
                    createWish(position, GiftInfo(), 0)
                }
            })

        roomWishAdapter.addOnDebouncedChildClick(
            R.id.tv_change, 500
        ) { _, _, position -> showSelectGift(position) }
    }

    private fun showSelectGift(position: Int) {
        SelectWishGiftPop.showDialog(context,
            object : SelectWishGiftPop.OnSelectWishListener {
                override fun onSelectGift(sumCount: Int, giftInfo: GiftInfo) {
                    DialogUtils.showLoading()
                    createWish(position, giftInfo, sumCount)
                }
            })
    }

    private fun createWish(
        position: Int,
        giftInfo: GiftInfo,
        sumCount: Int
    ) {
        val item = roomWishAdapter.getItem(position) ?: return
        val createWishRequest =
            CreateWishRequest(roomId, item.posId, giftInfo.id.toString(), sumCount)
        request({ agoraRxApi.setWishList(createWishRequest) },
            object : OnRequestResultListener<WishInfo> {
                override fun onSuccess(data: BaseBean<WishInfo>) {
                    DialogUtils.dismissLoading()
                    roomWishAdapter[position] = data.data!!
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    DialogUtils.dismissLoading()
                }
            })
    }

    interface OnSendGiftListener {
        fun onSendGift(giftInfo: GiftInfo)
    }
}
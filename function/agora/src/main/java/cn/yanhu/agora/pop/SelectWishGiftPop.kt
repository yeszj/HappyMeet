package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.GiftSelectItemAdapter
import cn.yanhu.agora.databinding.PopSelectWishGiftBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.view.GiftShowView
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2025/3/25
 * desc:
 */
@SuppressLint("ViewConstructor")
class SelectWishGiftPop(context: Context, val onSelectWishListener: OnSelectWishListener) :
    BottomPopupView(context) {
    private val selectGiftAdapter by lazy { GiftSelectItemAdapter() }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, onSelectWishListener: OnSelectWishListener
        ): SelectWishGiftPop {
            val matchPop = SelectWishGiftPop(mContext, onSelectWishListener)
            val builder = XPopup.Builder(mContext)
                .autoFocusEditText(false).autoOpenSoftInput(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_select_wish_gift
    }

    private lateinit var mBinding: PopSelectWishGiftBinding
    private var giftInfo: GiftResponse? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSelectWishGiftBinding.bind(popupImplView)

        initGiftAdapter()

        getGiftInfo()

        mBinding.tvChange.setOnSingleClickListener {
            val selectItem = selectGiftAdapter.getSelectItem()
            if (selectItem != null) {
                val checked = mBinding.radioGroup.checkedRadioButtonId
                var count = 0
                if (checked == -1) {
                    val toString = mBinding.etCustom.text.toString()
                    if (TextUtils.isEmpty(toString)) {
                        showToast("请输入自定义数量")
                    } else {
                        count = toString.toInt()
                    }
                } else {
                    when (checked) {
                        R.id.tv_one -> {
                            count = 1
                            selectGiftAdapter.setSelectPosition(0)
                        }

                        R.id.tv_five -> {
                            count = 5
                            selectGiftAdapter.setSelectPosition(1)
                        }

                        R.id.tv_ten -> {
                            count = 10
                            selectGiftAdapter.setSelectPosition(2)
                        }
                    }
                }
                if (count != 0){
                    onSelectWishListener.onSelectGift(count,selectItem)
                    clearInputCount()
                    dismiss()
                }

            }

        }
        changeSelectCount()
    }

    private fun changeSelectCount() {
        mBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId!=-1 && !isShowInput){
                clearInputCount()
            }else{
                isShowInput = false
            }
        }
        mBinding.radioGroup.check(mBinding.radioGroup.getChildAt(0).id)
        mBinding.etCustom.setOnSingleClickListener {
            showInputCount()
        }
    }

    private fun clearInputCount() {
        mBinding.etCustom.setBackgroundResource(R.drawable.bg_normal_wish_count)
        mBinding.etCustom.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        mBinding.etCustom.setText("")
        mBinding.etCustom.isFocusableInTouchMode = false
        mBinding.etCustom.isFocusable = false
        mBinding.etCustom.hint = "自定义"
        KeyboardUtils.hideSoftInput(mBinding.etCustom)
    }

    private var isShowInput = false
    private fun showInputCount() {
        isShowInput = true
        mBinding.etCustom.setBackgroundResource(R.drawable.bg_select_wish_count)
        mBinding.etCustom.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain))
        mBinding.radioGroup.clearCheck()
        mBinding.etCustom.requestFocus()
        mBinding.etCustom.hint = ""
        mBinding.etCustom.isFocusableInTouchMode = true
        mBinding.etCustom.isFocusable = true
        KeyboardUtils.showSoftInput(mBinding.etCustom)
    }

    private fun initGiftAdapter() {
        mBinding.rvGift.adapter = selectGiftAdapter
        selectGiftAdapter.setOnItemClickListener { _, _, position ->
            selectGiftAdapter.setSelectPosition(
                position
            )
            mBinding.rvGift.scrollToPosition(position)
        }
        if (!TextUtils.isEmpty(AppCacheManager.giftInfo)) {
            giftInfo = GsonUtils.fromJson(AppCacheManager.giftInfo, GiftResponse::class.java)
            removeRandomBoxGift()
            setGiftInfo()
        }
    }

    private fun getGiftInfo() {
        request(
            { imChatRxApi.getGiftList(GiftShowView.TYPE_GIFT) },
            object : OnRequestResultListener<GiftResponse> {
                override fun onSuccess(data: BaseBean<GiftResponse>) {
                    giftInfo = data.data
                    removeRandomBoxGift()
                    setGiftInfo()
                }
            })
    }

    private fun setGiftInfo() {
        selectGiftAdapter.submitList(giftInfo!!.list)
    }

    private fun removeRandomBoxGift() {
        //移除随机盲盒礼物
        giftInfo?.list?.removeIf {
            it.type == GiftInfo.TYPE_RANDOM_BOX
        }
    }

    interface OnSelectWishListener {
        fun onSelectGift( sumCount:Int,giftInfo: GiftInfo)
    }
}
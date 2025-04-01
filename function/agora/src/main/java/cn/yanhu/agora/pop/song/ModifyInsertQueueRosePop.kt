package cn.yanhu.agora.pop.song

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopModifyInsertQueueRoseBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/3/25
 * desc:k歌房间修改插队玫瑰数量
 */
@SuppressLint("ViewConstructor")
class ModifyInsertQueueRosePop(context: Context, val roomId:String, private val queuePrice:String): BottomPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, roomId:String,queuePrice:String
        ): ModifyInsertQueueRosePop {
            val matchPop = ModifyInsertQueueRosePop(mContext,roomId,queuePrice)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_modify_insert_queue_rose
    }

    private lateinit var mBinding: PopModifyInsertQueueRoseBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopModifyInsertQueueRoseBinding.bind(popupImplView)
        mBinding.tvPrice.text = "当前插队价格：${queuePrice}玫瑰"
        mBinding.etPrice.setText(queuePrice)
        mBinding.etPrice.setSelection(queuePrice.length)
        mBinding.tvModify.setOnSingleClickListener {
            resetQueuePrice()
        }
        mBinding.ivDismiss.setOnSingleClickListener { dismiss() }
    }

    private fun resetQueuePrice() {
        val inputPrice = mBinding.etPrice.text.toString()
        if (TextUtils.isEmpty(inputPrice)) {
            showToast("请输入价格")
        } else {
            request({ agoraRxApi.resetQueuePrice(roomId, inputPrice) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        showToast("重置成功")
                        dismiss()
                    }
                })

        }
    }
}
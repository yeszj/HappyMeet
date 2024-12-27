package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.FlowTagReasonAdapter
import cn.yanhu.agora.databinding.PopAdminCloseRoomReasonBinding
import cn.yanhu.baselib.utils.ext.showToast

/**
 * @author: zhengjun
 * created: 2023/12/6
 * desc:
 */
@SuppressLint("ViewConstructor")
class AdminCloseRoomReasonPop(
    context: Context,
    private val list: MutableList<String>,
    private val onCloseRoomListener: OnCloseRoomListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_admin_close_room_reason
    }

    private var mBinding: PopAdminCloseRoomReasonBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopAdminCloseRoomReasonBinding.bind(popupImplView)
        mBinding?.apply {
            ivClose.setOnClickListener {
                KeyboardUtils.hideSoftInput(etReason)
                dismiss()
            }
            btnSubmit.setOnClickListener {
                val selectedList = tagFlow.selectedList
                val inputReason = etReason.text.toString().trim()
                if (!TextUtils.isEmpty(inputReason)) {
                    onCloseRoomListener.onClose(inputReason)
                } else if (selectedList.isNotEmpty()) {
                    selectedList.forEach {
                        val s = list[it]
                        onCloseRoomListener.onClose(s)
                    }
                } else {
                    showToast("请选择关闭原因")
                }
            }
            tagFlow.setMaxSelectCount(1)
            tagFlow.adapter = FlowTagReasonAdapter(context, list)

            etReason.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s)) {
                        tagFlow.onChanged()
                    }
                }

            })
        }

    }


    interface OnCloseRoomListener{
        fun onClose(reason:String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            list: MutableList<String>,
            onCloseRoomListener: OnCloseRoomListener
        ): AdminCloseRoomReasonPop {
            val matchPop = AdminCloseRoomReasonPop(mContext,  list,onCloseRoomListener)
            val builder = XPopup.Builder(mContext)
            builder
                .autoOpenSoftInput(false)
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }
}
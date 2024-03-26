package cn.yanhu.dynamic.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.databinding.DialogInputContentBinding
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2023/7/3
 * desc:
 */
@SuppressLint("ViewConstructor")
class InputDiscussPop(
    context: Context,
    private val trendsId: String,
    private val commentId: String = "",
    private val secondLevelComment: String = "",
    private val userId: String = "",
    private val userName:String? = ""
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_input_content
    }

    private var mBinding: DialogInputContentBinding? = null
    override fun onCreate() {
        mBinding = DialogInputContentBinding.bind(popupImplView)
        mBinding?.apply {
            btnSend.setOnClickListener {
                //评论动态
                pubDiscuss(info = etContent.text.toString().trim())
            }
            if (!TextUtils.isEmpty(commentId)) {
                etContent.hint = "@$userName"
            } else {
                etContent.hint = "回复 $userName"
            }
        }

        checkInputContent()
    }

    private fun pubDiscuss(info: String) {
        val map = mutableMapOf<String, String>()
        map["trendsId"] = trendsId
        map["info"] = info
        if (!TextUtils.isEmpty(commentId)){
            map["commentId"] = commentId
        }
        if (!TextUtils.isEmpty(secondLevelComment)){
            map["secondLevelComment"] = secondLevelComment
        }
        if (!TextUtils.isEmpty(userId)){
            map["userId"] = userId
        }
        request(
            { momentRxApi.releaseComment(map) }, object : OnRequestResultListener<DiscussInfo> {
                override fun onSuccess(data: BaseBean<DiscussInfo>) {
                    val discussInfo = data.data
                    showToast("评论成功")
                    discussInfo?.apply {
                        this.isReply = !TextUtils.isEmpty(this@InputDiscussPop.userName)
                        this.commentId = trendsId
                        LiveDataEventManager.sendLiveDataMessage(
                            LiveDataEventManager.DISCUSS_SUCCESS,
                            this
                        )
                    }
                    dismiss()
                }
            }, activity = context as FragmentActivity
        )
    }


    private fun checkInputContent() {
        mBinding?.apply {
            etContent.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    super.onTextChanged(p0, p1, p2, p3)
                    if (TextUtils.isEmpty(p0)) {
                        btnSend.alpha = 0.5f
                        btnSend.isEnabled = false
                    } else {
                        btnSend.alpha = 1f
                        btnSend.isEnabled = true
                    }
                }
            })
        }
    }

    companion object {
        fun showInputDialog(
            context: Context,
            trendsId: String,
            commentId: String = "",
            secondLevelComment: String = "",
            userId: String = "",
            userName:String? = ""
        ) {
            val inputContentDialog = InputDiscussPop(context, trendsId,commentId,secondLevelComment,userId,userName)
            XPopup.Builder(ActivityUtils.getTopActivity())
                .autoFocusEditText(true)
                .autoOpenSoftInput(true)
                .asCustom(inputContentDialog).show()
        }
    }
}
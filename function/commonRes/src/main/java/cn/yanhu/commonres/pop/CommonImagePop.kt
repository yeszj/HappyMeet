package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.CommonEventPopInfo
import cn.yanhu.commonres.databinding.PopCommonImageBinding
import cn.yanhu.commonres.router.PageIntentUtil
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2025/1/17
 * desc:
 */
@SuppressLint("ViewConstructor")
class CommonImagePop(context: Context, private val commonEventPopInfo: CommonEventPopInfo,val resource:Drawable) :
    CenterPopupView(context) {
    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            commonEventPopInfo: CommonEventPopInfo,resource:Drawable,simpleCallback: SimpleCallback?=null
        ): CommonImagePop {
            val pop = CommonImagePop(context, commonEventPopInfo,resource)
            val builder = XPopup.Builder(context)
                .setPopupCallback(simpleCallback)
                .maxHeight(ScreenUtils.getScreenHeight())
                .maxWidth(ScreenUtils.getScreenWidth())
                .dismissOnBackPressed(commonEventPopInfo.clickDismiss)
                .dismissOnTouchOutside(commonEventPopInfo.clickDismiss)
            builder.asCustom(pop).show()
            return pop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_common_image
    }

    private lateinit var mBinding: PopCommonImageBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopCommonImageBinding.bind(popupImplView)
        mBinding.apply {
            if (commonEventPopInfo.imgWidth>0){
                ViewUtils.setViewWidth(ivPic,commonEventPopInfo.imgWidth)
            }
            ivPic.setImageDrawable(resource)
            ivPic.setOnSingleClickListener {
                if (!TextUtils.isEmpty(commonEventPopInfo.pageUrl)) {
                    PageIntentUtil.url2Page(context, commonEventPopInfo.pageUrl)
                }
                dismiss()
            }
        }
    }
}
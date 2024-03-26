package cn.yanhu.commonres.pop

import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.R
import cn.yanhu.commonres.databinding.PopUploadAvatarBinding
import cn.yanhu.commonres.manager.ImageSelectManager
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import java.util.ArrayList

/**
 * @author: zhengjun
 * created: 2024/3/1
 * desc:
 */
class UploadAvatarPop(val mContext: FragmentActivity, val isMan: Boolean,val onSelectPicResultListener: OnSelectPicResultListener) :
    BottomPopupView(mContext) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_upload_avatar
    }

    private var mBinding: PopUploadAvatarBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopUploadAvatarBinding.bind(popupImplView)
        mBinding?.apply {
            if (isMan) {
                ivExample.setImageResource(R.mipmap.avatar_example_man)
            } else {
                ivExample.setImageResource(R.mipmap.avatar_example_woman)
            }
            viewPic.setOnSingleClickListener {
                selectPic(ImageSelectUtils.TYPE_IMAGE)
            }
            viewCamera.setOnSingleClickListener {
               selectPic(ImageSelectUtils.TYPE_CAMERA)
            }
        }
    }

    private fun selectPic(type:Int){
        ImageSelectManager.selectPic(
            mContext,
            isCrop = true,
            type = type,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    if (!result.isNullOrEmpty()){
                        val localMedia = result[0]
                        val availablePath = localMedia.availablePath
                        onSelectPicResultListener.onPicPath(availablePath)
                        dismiss()
                    }
                }
                override fun onCancel() {
                }
            })
    }

    interface OnSelectPicResultListener{
        fun onPicPath(availablePath:String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            isMan: Boolean,
            onSelectPicResultListener: OnSelectPicResultListener
        ): UploadAvatarPop {
            val pop = UploadAvatarPop(mContext,isMan,onSelectPicResultListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(pop).show()
            return pop
        }
    }
}
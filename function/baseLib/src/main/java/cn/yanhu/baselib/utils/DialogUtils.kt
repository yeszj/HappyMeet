package cn.yanhu.baselib.utils

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ThreadUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnCancelListener
import com.lxj.xpopup.interfaces.OnSelectListener
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxj.xpopup.interfaces.OnImageViewerLongPressListener
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import cn.yanhu.baselib.R
import cn.yanhu.baselib.bean.AttachParamsInfo
import cn.yanhu.baselib.pop.CommonAttachListPopupView
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.ClipboardUtils
import com.lxj.xpopup.interfaces.XPopupCallback

/**
 * @author: witness
 * created: 2022/4/28
 * desc:
 */
object DialogUtils {
    const val animDuration = 150

    fun showCustomAttachViewPop(
        context: Context,
        view: View,
        textList: MutableList<AttachParamsInfo>,
        offsetX: Int = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_100),
        hasShadowBg: Boolean = true,
        onSelectListener: CommonAttachListPopupView.OnAttachSelectListener,
        xPopupCallback: XPopupCallback? = null
    ) {
        val attachListPopupView = CommonAttachListPopupView(ActivityUtils.getTopActivity())
            .setStringData(textList)
            .setOnSelectListener(onSelectListener)
        XPopup.Builder(context)
            .atView(view) // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .borderRadius(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10).toFloat())
            .animationDuration(animDuration)
            .offsetX(offsetX)
            .hasShadowBg(hasShadowBg)
            .setPopupCallback(xPopupCallback)
            .asCustom(attachListPopupView)
            .show()
    }


    fun showLongClickCopyDialog(
        view: View,
        copyValue: String,
        deleteListener: OnDeleteListener? = null
    ) {
        val list = mutableListOf<AttachParamsInfo>()
        list.add(
            AttachParamsInfo(
                "复制",
                R.drawable.icon_copy_black
            )
        )
        list.add(
            AttachParamsInfo(
                "删除",
                R.drawable.icon_red_delete,
                itemColor = R.color.fontRedColor
            )
        )
        showCustomAttachViewPop(
            ActivityUtils.getTopActivity(),
            view,
            list,
            onSelectListener = { position, _ ->
                if (position == 0) {
                    ClipboardUtils.copyText(copyValue)
                    showToast("复制成功")
                } else {
                    deleteListener?.startDelete()
                }
            })
    }

    fun showCustomCenterViewPop(
        context: Context,
        textList: MutableList<AttachParamsInfo>,
        onSelectListener: CommonAttachListPopupView.OnAttachSelectListener,
    ) {
        val attachListPopupView = CommonAttachListPopupView(ActivityUtils.getTopActivity())
            .setStringData(textList)
            .setOnSelectListener(onSelectListener)
        XPopup.Builder(context)
            .borderRadius(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10).toFloat())
            .animationDuration(animDuration)
            .asCustom(attachListPopupView)
            .show()
    }

    fun showAttachViewDialog(
        context: Context,
        view: View,
        textList: Array<String>,
        iconList: IntArray,
        offsetX: Int = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10),
        onSelectListener: OnSelectListener,
    ) {
        XPopup.Builder(context)
            .atView(view) // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .borderRadius(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_10).toFloat())
            .animationDuration(animDuration)
            .offsetX(offsetX)
            .asAttachList(
                textList,
                iconList,
                onSelectListener,
                0,
                R.layout.layout_common_attach_item
            ).setContentGravity(Gravity.START)
            .show()
    }


    @JvmStatic
    fun showConfirmDialog(
        title: CharSequence,
        onConfirmListener: OnConfirmListener? = null,
        content: CharSequence = "",
        confirm: String? = CommonUtils.getString(R.string.confirm),
    ): BasePopupView {
       return showConfirmDialog(title,onConfirmListener,content=content, isHideCancel = true, cancel = "", confirm = confirm)
    }


    @JvmStatic
    fun showConfirmDialog(
        title: CharSequence,
        onConfirmListener: OnConfirmListener? = null,
        onCancelListener: OnCancelListener? = null,
        content: CharSequence = "",
        cancel: String? = CommonUtils.getString(R.string.cancel),
        confirm: String? = CommonUtils.getString(R.string.confirm),
        isHideCancel: Boolean = false,
        isAutoDismiss: Boolean = true,
        confirmBg: Int = R.drawable.shape_common_btn_r30,
        cancelBg: Int = R.drawable.shape_transparent,
        context: Context = ActivityUtils.getTopActivity()
    ): BasePopupView {
        val asConfirm = XPopup.Builder(context)
            .autoDismiss(isAutoDismiss)
            .dismissOnTouchOutside(true)
            .dismissOnBackPressed(true)
            .asConfirm(
                title,
                content,
                cancel,
                confirm,
                onConfirmListener,
                onCancelListener, isHideCancel,
                R.layout.dialog_common_center_confirm
            )
        asConfirm.cancelTextView.setBackgroundResource(cancelBg)
        asConfirm.confirmTextView.setBackgroundResource(confirmBg)
        return asConfirm.show()
    }


    fun showCenterListDialog(
        title: String,
        data: Array<String>,
        onSelectListener: OnSelectListener,
    ) {
        XPopup.Builder(ActivityUtils.getTopActivity()) //.maxWidth(600)
            .asCenterList(
                title,
                data,
                null,
                -1,
                onSelectListener,
                R.layout.dialog_center_impl_list,
                R.layout.adapter_dialog_center_list_item
            )
            .show()
    }

    private var loadingDialog: BasePopupView? = null

    @JvmStatic
    fun showLoading(title: String? = CommonUtils.getString(R.string.loading)): BasePopupView? {
        if (loadingDialog == null || !loadingDialog!!.isShow) {
            loadingDialog = XPopup.Builder(ActivityUtils.getTopActivity())
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(true)
                .isDestroyOnDismiss(true)
                .asLoading(title)
                .show()
        }
        return loadingDialog
    }

    @JvmStatic
    fun dismissLoading() {
        ThreadUtils.runOnUiThread {
            if (loadingDialog != null && loadingDialog!!.isShow) {
                loadingDialog?.dismiss()
                loadingDialog = null
            }
        }
    }

    fun showImageViewerDialog(
        imageView: ImageView,
        url: String?,
        longPressListener: OnImageViewerLongPressListener? = null
    ) {
        if (!TextUtils.isEmpty(url)) {
            val asImageViewer = XPopup.Builder(ActivityUtils.getTopActivity())
                .asImageViewer(imageView, url, SmartGlideImageLoader())
            asImageViewer.isShowSaveButton(false)
            longPressListener?.apply {
                asImageViewer.setLongPressListener(this)
            }
            asImageViewer.show()
        }

    }

    fun showImageViewerDialog(
        imageView: ImageView?,
        position: Int,
        list: MutableList<String>,
        srcViewUpdateListener: OnSrcViewUpdateListener? = null
    ) {
        val asImageViewer = XPopup.Builder(ActivityUtils.getTopActivity())
            .isDestroyOnDismiss(true)
            .asImageViewer(
                imageView,
                position,
                list as List<Any>?,
                srcViewUpdateListener,
                SmartGlideImageLoader()
            )
        asImageViewer.isShowIndicator(true)
        asImageViewer.isShowSaveButton(false)
        asImageViewer.show()

    }

    interface OnDeleteListener {
        fun startDelete()
    }
}
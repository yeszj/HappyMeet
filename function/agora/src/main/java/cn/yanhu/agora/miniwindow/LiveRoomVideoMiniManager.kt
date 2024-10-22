package cn.yanhu.agora.miniwindow

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.databinding.DialogCallFloatLayoutBinding
import cn.yanhu.imchat.manager.CutLiveRoomUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ScreenUtils
import com.yhao.floatwindow.*

/**
 * 视频聊天小窗口
 */
class LiveRoomVideoMiniManager {
    private lateinit var miniWindows: View

    @SuppressLint("InflateParams")
    fun show(
        context: Activity,
        type: Int,
        onwnerInfo: UserDetailInfo?,
        permissionListener: PermissionListener,
        surfaceView: View?
    ) {
        if (isShowing || surfaceView == null) {
            return
        }
        miniWindows = LayoutInflater.from(context.applicationContext)
            .inflate(cn.yanhu.imchat.R.layout.dialog_call_float_layout, null)
        val binding = DialogCallFloatLayoutBinding.bind(miniWindows)

        val height = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_130)
        val width = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_100)
        val layoutParams = binding.floatSf.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        binding.floatSf.layoutParams = layoutParams
        if (binding.floatSf.childCount > 0) {
            binding.floatSf.removeAllViews()
        }
        val floatWindow = FloatWindow.get(TAG)
        if (floatWindow != null && floatWindow.isShowing) {
            return
        }
        ViewUtils.removeViewFormParent(surfaceView)
        binding.floatSf.addView(surfaceView)
        FloatWindow.with(context.applicationContext)
            .setTag(TAG)
            .setView(miniWindows)
            .setWidth(width)
            .setHeight(height)
            .setX(ScreenUtils.getScreenWidth() - width)
            .setMoveType(MoveType.slide)
            .setDesktopShow(true)
            .setPermissionListener(permissionListener)
            .build()
        onwnerInfo?.apply {
            GlideUtils.loadBlurTransPic(context,onwnerInfo.portrait, imageView = binding.ivAvatar)
        }
        miniWindows.setOnClickListener {
            binding.floatFunction.visibility =
                if (binding.floatFunction.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        binding.floatVideoLl.visibility = View.VISIBLE
        binding.floatWmode.setOnSingleClickListener {
            if (!CutLiveRoomUtils.isShow()) {
                closeFloat(type, false)
            }
        }
        binding.floatFinish.setOnSingleClickListener {
            if (AppUtils.isAppForeground() && onwnerInfo?.userId != AppCacheManager.userId) {
                closeFloat(type, true)
            } else {
                MiniWindowManager.switchLiveToFront(type, true)
                closeMiniWindow()
            }
        }
        val iFloatWindow = FloatWindow.get(TAG)
        if (iFloatWindow != null && !iFloatWindow.isShowing) {
            iFloatWindow.show()
        }
    }

    fun closeFloat(type: Int, isFinish: Boolean = false) {
        if (isFinish) {
            CutLiveRoomUtils.showChangeAlert(object : CutLiveRoomUtils.ChangeListener {
                override fun sure() {
                }
            })
        } else {
            MiniWindowManager.switchLiveToFront(type)
            closeMiniWindow()
        }
    }

    fun closeMiniWindow() {
        try {
            FloatWindow.destroy(TAG)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    val isShowing: Boolean
        get() {
            val iFloatWindow = FloatWindow.get(TAG) ?: return false
            return iFloatWindow.isShowing
        }

    companion object {
        const val TAG = "zegoVideoMiniManager"

        @JvmStatic
        fun getInstance() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        @SuppressLint("StaticFieldLeak")
        var sSingle = LiveRoomVideoMiniManager()
    }
}
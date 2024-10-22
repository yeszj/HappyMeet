package cn.yanhu.agora.ui.imphone

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.DialogFloatVideoPhoneBinding
import cn.yanhu.agora.manager.AgoraPhoneManager
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.GlideUtils.loadBlurTransPic
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.bean.UserDetailInfo

class VideoPhoneFloatView(activity: Activity) : LinearLayout(activity) {
    private val binding: DialogFloatVideoPhoneBinding
    private val dpi: Int
    private val screenHeight: Int
    private val screenWidth: Int
    private val wmParams: WindowManager.LayoutParams
    private val wm: WindowManager
    private var x = 0f
    private var y = 0f
    private var mTouchStartX = 0f
    private var mTouchStartY = 0f
    private var isScroll = false
    var videoView: View
    var isLocal = true

    init {
        videoView = LayoutInflater.from(activity).inflate(R.layout.dialog_float_video_phone, null)
        binding = DialogFloatVideoPhoneBinding.bind(videoView)
        ViewUtils.setViewCorner(binding.callFromVideoSf, 40)
        wm = activity.windowManager

        //通过像素密度来设置按钮的大小
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        dpi = dpi(dm.densityDpi)
        //屏宽
        screenWidth = wm.defaultDisplay.width
        //屏高
        screenHeight = wm.defaultDisplay.height

        //布局设置
        wmParams = WindowManager.LayoutParams()
        // 设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION
        wmParams.format = PixelFormat.RGBA_8888 // 设置图片格式，效果为背景透明
        wmParams.gravity = Gravity.LEFT or Gravity.TOP
        // 设置Window flag
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        wmParams.width = LayoutParams.WRAP_CONTENT
        wmParams.height = LayoutParams.WRAP_CONTENT
        wmParams.x = screenHeight - dpi shr 1
        wmParams.y = screenHeight / 3 * 2
        //创建一个View对象，作为悬浮窗的内容
        wm.addView(videoView, wmParams)
        videoView.setOnTouchListener(object : OnTouchListener {
            var result = false
            var left = 0
            var top = 0
            var startX = 0f
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // 获取相对屏幕的坐标， 以屏幕左上角为原点
                x = event.rawX
                y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        result = false
                        startX = event.rawX
                        startY = event.rawY

//                        left = layoutParams.x;
//                        top = layoutParams.y;
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = event.x
                        mTouchStartY = event.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (Math.abs(event.rawX - startX) > 20 || Math.abs(event.rawY - startY) > 20) {
                            result = true
                        }
                        if (isScroll) {
                            updateViewPosition()
                        } else {
                            // 当前不处于连续滑动状态 则滑动小于图标1/3则不滑动
                            if (Math.abs(mTouchStartX - event.x) > dpi / 3
                                || Math.abs(mTouchStartY - event.y) > dpi / 3
                            ) {
                                updateViewPosition()
                            }
                        }
                        isScroll = true
                    }

                    MotionEvent.ACTION_UP -> {
                        // 拖动
                        if (isScroll) {
                            autoView()
                            // setBackgroundDrawable(closeDrawable);
                            // invalidate();
                        } else {
                            // 当前显示功能区，则隐藏
                            // setBackgroundDrawable(openDrawable);
                            // invalidate();
                        }
                        isScroll = false
                        run {
                            mTouchStartY = 0f
                            mTouchStartX = mTouchStartY
                        }
                    }
                }
                return result
            }
        })
    }



    @SuppressLint("ClickableViewAccessibility")
    fun updataView(activity: Activity?, data: UserDetailInfo, isLocal: Boolean) {
        this.isLocal = isLocal
        if (isLocal) {
            AgoraPhoneManager.getInstance().setupLocalVideo(binding.callFromVideoSf)
        } else {
            AgoraPhoneManager.getInstance()
                .setupRemoteVideo(data.id.toInt(), binding.callFromVideoSf)
        }
        loadBlurTransPic(context, data.portrait, 20, 4, binding.fromBgPreload)

        GlideUtils.loadImage(context, data.portrait,binding.callFromUserPortrait)
        isShowVideo(data.colseVideo)
    }

    /**
     * 根据密度选择控件大小
     */
    private fun dpi(densityDpi: Int): Int {
        if (densityDpi <= 120) {
            return 36
        } else if (densityDpi <= 160) {
            return 48
        } else if (densityDpi <= 240) {
            return 72
        } else if (densityDpi <= 320) {
            return 96
        }
        return 108
    }

    fun show() {
        if (isShown) {
            return
        }
        visibility = VISIBLE
    }

    fun hide() {
        visibility = GONE
    }

    fun destory() {
        hide()
        try {
            wm.removeViewImmediate(videoView)
        } catch (e: Exception) {
        }
    }

    /**
     * 自动移动位置
     */
    private fun autoView() {
        // 得到view在屏幕中的位置
        val location = IntArray(2)
        videoView.getLocationOnScreen(location)
        //左侧
        if (location[0] < screenWidth / 2 - width / 2) {
            updateViewPosition(LEFT)
        } else {
            updateViewPosition(RIGHT)
        }
    }

    /**
     * 手指释放更新悬浮窗位置
     */
    private fun updateViewPosition(l: Int) {
        when (l) {
            LEFT -> wmParams.x = 0
            RIGHT -> {
                val x = screenWidth - dpi
                wmParams.x = x
            }

            TOP -> wmParams.y = 0
            BUTTOM -> wmParams.y = screenHeight - dpi
        }
        wm.updateViewLayout(videoView, wmParams)
    }

    // 更新浮动窗口位置参数
    private fun updateViewPosition() {
        wmParams.x = (x - mTouchStartX).toInt()
        //是否存在状态栏（提升滑动效果）
        // 不设置为全屏（状态栏存在） 标题栏是屏幕的1/25
        wmParams.y = (y - mTouchStartY - screenHeight / 25).toInt()
        wm.updateViewLayout(videoView, wmParams)
    }

    //是否打开摄像头
    fun isShowVideo(isClose: Boolean) {
        binding.callFromVideoSf.visibility = if (isClose) INVISIBLE else VISIBLE
        binding.fromBgPreload.visibility = if (isClose) VISIBLE else GONE
        binding.callFromUserPortrait.visibility = if (isClose) VISIBLE else GONE
    }

    companion object {
        // 悬浮栏位置
        private const val LEFT = 0
        private const val RIGHT = 1
        private const val TOP = 3
        private const val BUTTOM = 4
    }
}
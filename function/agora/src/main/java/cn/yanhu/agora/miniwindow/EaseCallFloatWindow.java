package cn.yanhu.agora.miniwindow;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.hyphenate.util.EMLog;

import cn.yanhu.agora.databinding.DialogPhoneFloatLayoutBinding;
import cn.yanhu.agora.R;
import cn.yanhu.agora.manager.AgoraPhoneManager;
import cn.yanhu.baselib.utils.ViewUtils;
/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 02/06/2021
 */
public class EaseCallFloatWindow {
    private static final String TAG = "EaseCallFloatWindow";

    private Context context;

    private static EaseCallFloatWindow instance;

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams layoutParams = null;

    private View floatView;
    private int screenWidth;
    private int floatViewWidth;
    private int callType;//0：语音  1：视频

    private DialogPhoneFloatLayoutBinding binding;

    private EaseCallFloatWindow() {
    }

    public static EaseCallFloatWindow getInstance() {
        if (instance == null) {
            synchronized (EaseCallFloatWindow.class) {
                if (instance == null) {
                    instance = new EaseCallFloatWindow();
                }
            }
        }
        return instance;
    }

    public void initFloatWindow(Context context, int callType) {
        this.context = context;
        this.callType = callType;
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
    }

    /**
     * add float window
     */
    public void show(int uid) { //
        if (floatView != null) {
            return;
        }
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.format = PixelFormat.TRANSPARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

        floatView = LayoutInflater.from(context).inflate(R.layout.dialog_phone_float_layout, null);
//        floatView.setFocusableInTouchMode(true);
        binding = DialogPhoneFloatLayoutBinding.bind(floatView);

        windowManager.addView(floatView, layoutParams);
        floatView.post(new Runnable() {
            @Override
            public void run() {
                // Get the size of floatView;
                if (floatView != null) {
                    floatViewWidth = floatView.getWidth();
                }
            }
        });
        if (callType == 1) {
            ViewUtils.INSTANCE.setViewCorner(binding.floatSf, 40);
            AgoraPhoneManager.getInstance().setupRemoteVideo(uid, binding.floatSf);
        }
        floatView.setOnTouchListener(new View.OnTouchListener() {
            boolean result = false;

            int left;
            int top;
            float startX = 0;
            float startY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        result = false;
                        startX = event.getRawX();
                        startY = event.getRawY();

                        left = layoutParams.x;
                        top = layoutParams.y;

                        EMLog.i(TAG, "startX: " + startX + ", startY: " + startY + ", left: " + left + ", top: " + top);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - startX) > 20 || Math.abs(event.getRawY() - startY) > 20) {
                            result = true;
                        }

                        int deltaX = (int) (startX - event.getRawX());

                        layoutParams.x = left + deltaX;
                        layoutParams.y = (int) (top + event.getRawY() - startY);
                        EMLog.i(TAG, "startX: " + (event.getRawX() - startX) + ", startY: " + (event.getRawY() - startY)
                                + ", left: " + left + ", top: " + top);
                        windowManager.updateViewLayout(floatView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        smoothScrollToBorder();
                        break;
                }
                return result;
            }
        });

        floatView.setOnClickListener(v -> {
            if (callType == 0) {
                Activity topActivity = ActivityUtils.getTopActivity();
                MiniWindowManager.INSTANCE.switchVoiceToFront(topActivity);
                dismiss();
            } else {
                binding.floatFunction.setVisibility(binding.floatFunction.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }

        });

        if (callType == 0) {
            binding.floatVoiceLl.setVisibility(View.VISIBLE);

        } else {
            binding.floatVideoLl.setVisibility(View.VISIBLE);
            binding.floatWmode.setOnClickListener(v -> {
                Activity topActivity = ActivityUtils.getTopActivity();
                MiniWindowManager.switchVideoToFront(topActivity,false);
                dismiss();
            });

            binding.floatFinish.setOnClickListener(v -> {
                Activity topActivity = ActivityUtils.getTopActivity();
                MiniWindowManager.switchVideoToFront(topActivity,true);
                dismiss();
            });
        }

    }

    public boolean isShowing() {
        return floatView != null;
    }

    public int getCallType() {
        return callType;
    }

    public void setTime(String timeStr) {
        binding.setTimerStr(timeStr);
    }

    /**
     * 停止悬浮窗
     */
    public void dismiss() {
        Log.i(TAG, "dismiss: ");
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
        }
        binding.unbind();
        binding = null;
        floatView = null;
    }

    private void smoothScrollToBorder() {
        EMLog.i(TAG, "screenWidth: " + screenWidth + ", floatViewWidth: " + floatViewWidth);
        int splitLine = screenWidth / 2 - floatViewWidth / 2;
        final int left = layoutParams.x;
        final int top = layoutParams.y;
        int targetX;

        if (left < splitLine) {
            // 滑动到最左边
            targetX = 0;
        } else {
            // 滑动到最右边
            targetX = screenWidth - floatViewWidth;
        }

        ValueAnimator animator = ValueAnimator.ofInt(left, targetX);
        animator.setDuration(100)
                .addUpdateListener(animation -> {
                    if (floatView == null) return;

                    int value = (int) animation.getAnimatedValue();
                    EMLog.i(TAG, "onAnimationUpdate, value: " + value);
                    layoutParams.x = value;
                    layoutParams.y = top;
                    windowManager.updateViewLayout(floatView, layoutParams);
                });
        animator.start();
    }
}

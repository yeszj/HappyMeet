package cn.yanhu.baselib.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DragConstraintLayout extends ConstraintLayout {

    private int parentHeight;
    private int parentWidth;

    private int lastX;
    private int lastY;

    public DragConstraintLayout(Context context) {
        super(context);
    }

    public DragConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int downX;
    private int downY;
    private long downTime;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                this.setAlpha(0.9f);
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX = rawX;
                lastY = rawY;

                downX = rawX;
                downY = rawY;
                downTime = System.currentTimeMillis();
                if (getParent() != null) {
                    ViewGroup parent = (ViewGroup) getParent();
                    parentHeight = parent.getHeight();
                    parentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (parentHeight <= 0.2 || parentWidth <= 0.2) {
                    break;
                }
                this.setAlpha(0.9f);
                int dx = rawX - lastX;
                int dy = rawY - lastY;
                //这里修复一些华为手机无法触发点击事件
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                if (distance == 0) {
                    break;
                }
                float x = getX() + dx;
                float y = getY() + dy;
                //检测是否到达边缘 左上右下
                x = x < 0 ? 0 : x + getWidth() > parentWidth ? parentWidth - getWidth() : x;
                y = y < 0 ? 0 : y + getHeight() > parentHeight ? parentHeight - getHeight() : y;
                setX(x);
                setY(y);
                lastX = rawX;
                lastY = rawY;
                Log.i("ACTION_MOVE", "getX=" + getX() + ";getY=" + getY() + ";parentWidth=" + parentWidth + ";parentHeight=" + parentHeight);
                break;
            case MotionEvent.ACTION_UP:
                moveHide(rawX);
                this.setAlpha(1.0f);
                if (onClickListener != null
                        && Math.abs(downX - rawX) < 10
                        && Math.abs(downY - rawY) < 10
                        && System.currentTimeMillis() - downTime < 1000) {
                    Log.i("ACTION_UP", "触发点击事件 ");
                    onClickListener.onClick(this);
                }
                break;
        }
        return true;
    }

    OnClickListener onClickListener;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    private void moveHide(int rawX) {
        if (rawX >= parentWidth / 2) {
            //靠右吸附
            animate().setInterpolator(new DecelerateInterpolator())
                    .setDuration(500)
                    .xBy(parentWidth - getWidth() - getX())
                    .start();
        } else {
            //靠左吸附
            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0);
            oa.setInterpolator(new DecelerateInterpolator());
            oa.setDuration(500);
            oa.start();

        }
    }
}

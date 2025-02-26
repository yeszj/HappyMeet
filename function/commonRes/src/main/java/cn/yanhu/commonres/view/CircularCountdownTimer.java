package cn.yanhu.commonres.view;

/**
 * @author: zhengjun
 * created: 2025/2/14
 * desc:
 */
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cn.yanhu.commonres.R;

public class CircularCountdownTimer extends View {

    private Paint backgroundPaint; // 背景圆环画笔
    private Paint progressPaint;   // 进度圆环画笔
    private RectF rectF;          // 绘制圆环的矩形区域
    private float progress = 360; // 初始进度（360度）
    private long countdownTime = 5000; // 默认倒计时时间（5秒）
    private ValueAnimator countdownAnimator;

    // 自定义属性
    private int backgroundColor; // 背景颜色
    private int progressColor;  // 进度颜色
    private float strokeWidth;  // 圆环宽度

    public CircularCountdownTimer(Context context) {
        super(context);
        init(null);
    }

    public CircularCountdownTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularCountdownTimer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // 获取自定义属性
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircularCountdownTimer);
            backgroundColor = typedArray.getColor(R.styleable.CircularCountdownTimer_background_color, 0xFFCCCCCC);
            progressColor = typedArray.getColor(R.styleable.CircularCountdownTimer_circle_progress_color, 0xFFFF0000);
            strokeWidth = typedArray.getDimension(R.styleable.CircularCountdownTimer_stroke_width, 10);
            typedArray.recycle();
        } else {
            backgroundColor = 0xFFCCCCCC;
            progressColor = 0xFFFF0000;
            strokeWidth = 10;
        }

        // 初始化背景圆环画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);

        // 初始化进度圆环画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);

        // 初始化矩形区域
        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算圆形进度条的位置
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - strokeWidth / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // 绘制背景圆环
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // 绘制进度圆环（从-90度开始，顺时针绘制）
        canvas.drawArc(rectF, -90, -progress, false, progressPaint);
    }

    /**
     * 设置倒计时时间
     *
     * @param time 倒计时时间（毫秒）
     */
    public void setCountdownTime(long time) {
        this.countdownTime = time;
    }

    /**
     * 启动倒计时动画
     */
    public void startCountdown() {
        if (countdownAnimator != null && countdownAnimator.isRunning()) {
            countdownAnimator.cancel();
        }

        // 使用ValueAnimator实现动画
        countdownAnimator = ValueAnimator.ofFloat(360, 0);
        countdownAnimator.setDuration(countdownTime);
        countdownAnimator.setInterpolator(new LinearInterpolator());
        countdownAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate(); // 重绘View
        });
        countdownAnimator.start();
    }

    /**
     * 停止倒计时动画
     */
    public void stopCountdown() {
        if (countdownAnimator != null) {
            countdownAnimator.cancel();
        }
    }
}

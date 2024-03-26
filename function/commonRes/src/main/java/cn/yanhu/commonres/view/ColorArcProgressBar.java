package cn.yanhu.commonres.view;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import cn.yanhu.commonres.R;


/**
 * @author: witness
 * created: 2020-01-12
 * desc:
 */
public class ColorArcProgressBar extends View {
    private float diameter = 500; //直径
    private float centerX; //圆心X坐标
    private float centerY; //圆心Y坐标

    private Paint allArcPaint;
    private Paint progressPaint;
    private Paint vTextPaint;
    private Paint hintPaint;
    private Paint dotPaint;
    private Paint dotPaint1;
    private Paint degreePaint;
    private Paint curSpeedPaint;

    private RectF bgRect;

    private RectF borderRect;
    private RectF borderRect1;

    private PaintFlagsDrawFilter mDrawFilter;
    private SweepGradient sweepGradient;
    private Matrix rotateMatrix;

    private float sweepAngle = 270;
    private float currentAngle = 0;
    private int[] colors = new int[]{Color.GREEN, Color.YELLOW, Color.RED, Color.RED};
    private float maxValues = 60;
    private float curValues = 0;
    private float bgArcWidth = dipToPx(2);
    private float progressWidth = dipToPx(10);
    private float textSize = dipToPx(60);
    private float hintSize = dipToPx(15);
    private final float curSpeedSize = dipToPx(25);
    private static final int aniSpeed = 1000;
    private final float longdegree = dipToPx(13);
    private final float shortdegree = dipToPx(10); //控制刻度的高度
    private final int DEGREE_PROGRESS_DISTANCE = dipToPx(5);//控制刻度距离圆弧的距离

    private static final String hintColor = "#575757";
    private static final String contentColor = "#3CCDB1";
    private int longDegreeColor;
    private int bgArcColor; //圆圈背景色（画笔颜色）
    private String titleString;
    private String hintString;
    private int dotColor;
    private int startColor;
    private int endColor;
    private float dotWidth = dipToPx(16);
    private float dotSmallWidth = dipToPx(10);
    private boolean isNeedTitle;
    private boolean isNeedUnit;
    private boolean isNeedDial;
    private boolean isNeedEndDot;
    private boolean isNeedContent;
    private boolean isShowInfBorder;
    private boolean isShowOutBorder;
    protected int degreeNum;//刻度数量
    private final float borderDistance = dipToPx(15);
    private final float borderDistance1 = dipToPx(24);
    // sweepAngle / maxValues 的值
    private float k;
    private Paint borderPaint;
    private int borderColor;

    public ColorArcProgressBar(Context context) {
        super(context, null);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCofig(context, attrs);
        initView();
    }

    public ColorArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCofig(context, attrs);
        initView();
    }

    /**
     * 初始化布局配置
     */
    private void initCofig(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorArcProgressBar);
        int color1 = a.getColor(R.styleable.ColorArcProgressBar_front_color1, Color.GREEN);
        int color2 = a.getColor(R.styleable.ColorArcProgressBar_front_color2, color1);
        int color3 = a.getColor(R.styleable.ColorArcProgressBar_front_color3, color1);
        dotColor = a.getColor(R.styleable.ColorArcProgressBar_dot_color, Color.GREEN);
        startColor = a.getColor(R.styleable.ColorArcProgressBar_start_color, Color.GREEN);
        endColor = a.getColor(R.styleable.ColorArcProgressBar_end_color, Color.GREEN);
        borderColor = a.getColor(R.styleable.ColorArcProgressBar_border_color, Color.GREEN);
        longDegreeColor = a.getColor(R.styleable.ColorArcProgressBar_degree_color, color1);
        degreeNum = a.getInteger(R.styleable.ColorArcProgressBar_degree_num,40);
        colors = new int[]{color1, color2, color3, color3};

        sweepAngle = a.getInteger(R.styleable.ColorArcProgressBar_total_engle, 180);
        bgArcWidth = a.getDimension(R.styleable.ColorArcProgressBar_back_width, dipToPx(2));
        dotWidth = a.getDimension(R.styleable.ColorArcProgressBar_dot_width, dipToPx(5));
        dotSmallWidth = a.getDimension(R.styleable.ColorArcProgressBar_dot_small_width, dotWidth);
        progressWidth = a.getDimension(R.styleable.ColorArcProgressBar_front_width, dipToPx(10));
        isNeedTitle = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_title, false);
        isNeedContent = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_content, false);
        isShowInfBorder= a.getBoolean(R.styleable.ColorArcProgressBar_is_show_in_border, false);
        isShowOutBorder= a.getBoolean(R.styleable.ColorArcProgressBar_is_show_out_border, false);
        isNeedUnit = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_unit, false);
        isNeedDial = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_dial, false);
        isNeedEndDot = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_end_dot, false);
        hintString = a.getString(R.styleable.ColorArcProgressBar_string_unit);
        titleString = a.getString(R.styleable.ColorArcProgressBar_string_title);
        curValues = a.getFloat(R.styleable.ColorArcProgressBar_current_value, 0);
        maxValues = a.getFloat(R.styleable.ColorArcProgressBar_max_value, 60);
        diameter = a.getDimension(R.styleable.ColorArcProgressBar_circleWidth, 0);
        bgArcColor = a.getColor(R.styleable.ColorArcProgressBar_bg_arc_color, color1);
        setCurrentValues(curValues);
        setMaxValues(maxValues);
        a.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE);
        int height = (int) (2 * longdegree + progressWidth + (diameter * sweepAngle) / 360 + 2 * DEGREE_PROGRESS_DISTANCE);
        setMeasuredDimension(width, height);
    }

    private void initView() {
        if (diameter == 0) {
            diameter = 3 * getScreenWidth() / 5;
        }
//弧形的矩阵区域
        bgRect = new RectF();
        bgRect.top = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE;
        bgRect.left = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE;
        bgRect.right = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE);
        bgRect.bottom = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE);


        borderRect = new RectF();
        borderRect.top = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE - borderDistance;
        borderRect.left = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE - borderDistance;
        borderRect.right = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE) + borderDistance;
        borderRect.bottom = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE) + borderDistance;

        borderRect1 = new RectF();
        borderRect1.top = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE + borderDistance1;
        borderRect1.left = longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE + borderDistance1;
        borderRect1.right = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE) - borderDistance1;
        borderRect1.bottom = diameter + (longdegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE) - borderDistance1;

//圆心
        centerX = (2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE) / 2;
        centerY = (2 * longdegree + progressWidth + diameter + 2 * DEGREE_PROGRESS_DISTANCE) / 2;

//外部刻度线
        degreePaint = new Paint();
        degreePaint.setColor(longDegreeColor);


        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#ffffff"));

//整个弧形
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeWidth(bgArcWidth);
        allArcPaint.setColor(bgArcColor);
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);

        //外部边界线
        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dipToPx(1));
        borderPaint.setStrokeCap(Paint.Cap.ROUND);

//当前进度的弧形
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(bgArcWidth);
        progressPaint.setColor(Color.WHITE);
        LinearGradient lg = new LinearGradient(centerX - diameter / 2, centerY, centerX + diameter / 2, centerY - diameter / 2, startColor, endColor, Shader.TileMode.MIRROR);
        progressPaint.setShader(lg);
//内容显示文字
        vTextPaint = new Paint();
        vTextPaint.setTextSize(textSize);
        vTextPaint.setColor(Color.parseColor(contentColor));
        vTextPaint.setTextAlign(Paint.Align.CENTER);

//显示单位文字
        hintPaint = new Paint();
        hintPaint.setTextSize(hintSize);
        hintPaint.setColor(Color.parseColor(hintColor));
        hintPaint.setTextAlign(Paint.Align.CENTER);

//画进度调最后的小圆点
        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setColor(dotColor);
        dotPaint.setStrokeWidth(dotWidth);
        dotPaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint1 = new Paint();
        dotPaint1.setAntiAlias(true);
        dotPaint1.setStyle(Paint.Style.STROKE);
        dotPaint1.setColor(Color.WHITE);
        dotPaint1.setStrokeWidth(dotSmallWidth);
        dotPaint1.setStrokeCap(Paint.Cap.ROUND);

//显示标题文字
        curSpeedPaint = new Paint();
        curSpeedPaint.setTextSize(curSpeedSize);
        curSpeedPaint.setColor(Color.parseColor(hintColor));
        curSpeedPaint.setTextAlign(Paint.Align.CENTER);

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        sweepGradient = new SweepGradient(centerX, centerY, colors, null);
        rotateMatrix = new Matrix();

    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {
//抗锯齿
        float startAngle = 270 - sweepAngle / 2f;
        canvas.setDrawFilter(mDrawFilter);

        if (isNeedDial) {
            //画刻度线
            drawDial(canvas);
        }

        if (isShowInfBorder){
            //内边界
            canvas.drawArc(borderRect1, startAngle, sweepAngle, false, borderPaint);
        }
        if (isShowOutBorder){
            //外边界
            canvas.drawArc(borderRect, startAngle, sweepAngle, false, borderPaint);
        }

        //整个弧
        canvas.drawArc(bgRect, startAngle, sweepAngle, false, allArcPaint);

        //设置渐变色
        rotateMatrix.setRotate(130, centerX, centerY);
        sweepGradient.setLocalMatrix(rotateMatrix);

        //当前进度
        canvas.drawArc(bgRect, startAngle, currentAngle, false, progressPaint);

        if (isNeedEndDot) {
            // 计算转过的角度
            canvas.drawArc(bgRect, currentAngle + startAngle - 0.5f, 0.5f, false, dotPaint);
            canvas.drawArc(bgRect, currentAngle + startAngle - 0.5f, 0.5f, false, dotPaint1);
        }

        if (isNeedContent) {
            canvas.drawText(String.format("%.0f", curValues), centerX, centerY + textSize / 3, vTextPaint);
        }
        if (isNeedUnit) {
            canvas.drawText(hintString, centerX, centerY + 2 * textSize / 3, hintPaint);
        }
        if (isNeedTitle) {
            canvas.drawText(titleString, centerX, centerY - 2 * textSize / 3, curSpeedPaint);
        }


        invalidate();

    }


    private void drawDial(Canvas canvas) {
        for (int i = 0; i < degreeNum; i++) {
            float degrees = 360/degreeNum;
            int start = degreeNum / 4;
            if (i > start && i < start*3) {
                canvas.rotate(degrees, centerX, centerY);
                continue;
            }
            degreePaint.setStrokeWidth(dipToPx(1.0f));
            // degreePaint.setColor(Color.parseColor(shortDegreeColor));
            canvas.drawLine(
                    centerX, centerY - diameter / 2 + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE,
                    centerX, centerY - diameter / 2 + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE + shortdegree,
                    degreePaint);
//                }

            canvas.rotate(degrees, centerX, centerY);
        }
    }

    /**
     * 设置最大值
     */
    public void setMaxValues(float maxValues) {
        this.maxValues = maxValues;
        k = sweepAngle / maxValues;
    }

    /**
     * 设置当前值
     */
    public void setCurrentValues(float currentValues) {
        if (currentValues > maxValues) {
            currentValues = maxValues;
        }
        if (currentValues < 0) {
            currentValues = 0;
        }
        this.curValues = currentValues;
        float lastAngle = currentAngle;
        setAnimation(lastAngle, currentValues * k, aniSpeed);
    }

    /**
     * 设置整个圆弧宽度
     */
    public void setBgArcWidth(int bgArcWidth) {
        this.bgArcWidth = bgArcWidth;
    }

    /**
     * 设置进度宽度
     */
    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    /**
     * 设置速度文字大小
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * 设置单位文字大小
     */
    public void setHintSize(int hintSize) {
        this.hintSize = hintSize;
    }

    /**
     * 设置单位文字
     */
    public void setUnit(String hintString) {
        this.hintString = hintString;
        invalidate();
    }

    /**
     * 设置直径大小
     */
    public void setDiameter(int diameter) {
        this.diameter = dipToPx(diameter);
    }

    /**
     * 设置标题
     */
    private void setTitle(String title) {
        this.titleString = title;
    }

    /**
     * 设置是否显示标题
     */
    private void setIsNeedTitle(boolean isNeedTitle) {
        this.isNeedTitle = isNeedTitle;
    }

    /**
     * 设置是否显示单位文字
     */
    private void setIsNeedUnit(boolean isNeedUnit) {
        this.isNeedUnit = isNeedUnit;
    }

    /**
     * 设置是否显示外部刻度盘
     */
    private void setIsNeedDial(boolean isNeedDial) {
        this.isNeedDial = isNeedDial;
    }

    /**
     * 为进度设置动画
     */
    private void setAnimation(float last, float current, int length) {
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngle);
        progressAnimator.addUpdateListener(animation -> {
            currentAngle = (Float) animation.getAnimatedValue();
            curValues = currentAngle / k;
        });
        progressAnimator.start();
    }

    /**
     * dip 转换成px
     */
    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 得到屏幕宽度
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}


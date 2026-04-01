package cn.yanhu.baselib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import cn.yanhu.baselib.R;

public class GradientBorderCircleImageView extends AppCompatImageView {

    private Paint mPaint; // 绘制头像的画笔
    private Paint mBorderPaint; // 绘制边框的画笔
    private int mBorderWidth = 10; // 默认边框宽度
    private int mBorderStartColor = Color.RED; // 默认边框起始颜色
    private int mBorderEndColor = Color.YELLOW; // 默认边框结束颜色

    public GradientBorderCircleImageView(Context context) {
        super(context);
        init(null);
    }

    public GradientBorderCircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GradientBorderCircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        // 解析 XML 属性
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GradientBorderCircleImageView);
            mBorderWidth = a.getDimensionPixelSize(R.styleable.GradientBorderCircleImageView_fl_borderWidth, mBorderWidth);
            mBorderStartColor = a.getColor(R.styleable.GradientBorderCircleImageView_fl_borderStartColor, mBorderStartColor);
            mBorderEndColor = a.getColor(R.styleable.GradientBorderCircleImageView_fl_borderEndColor, mBorderEndColor);
            a.recycle();
        }

        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap == null) {
            super.onDraw(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        // 创建圆形 BitmapShader
        @SuppressLint("DrawAllocation") BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(bitmap, size, size, false),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);

        // 计算圆形头像的半径
        float radius = (size - mBorderWidth) / 2f;

        // 绘制圆形头像
        canvas.drawCircle(width / 2f, height / 2f, radius, mPaint);

        // 更新边框渐变色
        @SuppressLint("DrawAllocation") Shader borderShader = new LinearGradient(
                0, 0, width, height, // 渐变的起始和结束坐标
                new int[]{mBorderStartColor, mBorderEndColor}, // 渐变色数组
                null, Shader.TileMode.CLAMP // 平铺模式
        );
        mBorderPaint.setShader(borderShader);

        // 绘制边框
        canvas.drawCircle(width / 2f, height / 2f, radius, mBorderPaint);
    }

    public void setBorderWidth(int borderWidth) {
        this.mBorderWidth = borderWidth;
        mBorderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    public void setBorderColors(int startColor, int endColor) {
        this.mBorderStartColor = startColor;
        this.mBorderEndColor = endColor;
        invalidate();
    }
}

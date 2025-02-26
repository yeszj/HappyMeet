package com.pcl.sdklib.sdk.faceAuth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.pcl.sdklib.R;

public class Overlay extends View {

    private static final int SHAPE_CIRCLE = 0;

    private static final int SHAPE_RECT = 1;

    private final Path mPath = new Path();

    private int mMaskColor = Color.WHITE;

    private int mShape = SHAPE_CIRCLE;

    private float mTargetRestrictCircleRadius = 0;

    private float mTargetRestrictRectWidth = 0;

    private float mTargetRestrictRectHeight = 0;

    private float mTargetRestrictRectRoundRadius = 0;

    public Overlay(Context context) {
        super(context);
        init(null, 0);
    }

    public Overlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Overlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Overlay, defStyle, 0);

        mMaskColor = a.getColor(R.styleable.Overlay_mask_color, mMaskColor);
        mShape = a.getInt(R.styleable.Overlay_target_restrict_area_shape, SHAPE_CIRCLE);
        mTargetRestrictCircleRadius = a.getDimension(R.styleable.Overlay_target_restrict_circle_radius, 0);
        mTargetRestrictRectWidth = a.getDimension(R.styleable.Overlay_target_restrict_rect_width, 0);
        mTargetRestrictRectHeight = a.getDimension(R.styleable.Overlay_target_restrict_rect_height, 0);
        mTargetRestrictRectRoundRadius = a.getDimension(R.styleable.Overlay_target_restrict_rect_round_radius, 0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        mPath.reset();

        switch (mShape) {
            case SHAPE_CIRCLE:
                mPath.addCircle(contentWidth / 2F, contentHeight / 2F, mTargetRestrictCircleRadius, Path.Direction.CCW);
                break;
            case SHAPE_RECT:
                float left = (contentWidth - mTargetRestrictRectWidth) / 2F;
                float top = (contentHeight - mTargetRestrictRectHeight) / 2F;
                mPath.addRoundRect(left, top, left + mTargetRestrictRectWidth, top + mTargetRestrictRectHeight, mTargetRestrictRectRoundRadius, mTargetRestrictRectRoundRadius, Path.Direction.CCW);
                break;
            default:
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(mPath);
            canvas.drawColor(mMaskColor);
        } else {
            canvas.clipPath(mPath, Region.Op.DIFFERENCE);
            canvas.drawColor(mMaskColor);
        }
    }

    public RectF getTargetRestrictArea() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        float centerX = contentWidth / 2F;
        float centerY = contentHeight / 2F;

        switch (mShape) {
            case SHAPE_CIRCLE:
                return new RectF(centerX - mTargetRestrictCircleRadius, centerY - mTargetRestrictCircleRadius,
                        centerX + mTargetRestrictCircleRadius, centerY + mTargetRestrictCircleRadius);
            case SHAPE_RECT:
                float left = (contentWidth - mTargetRestrictRectWidth) / 2F;
                float top = (contentHeight - mTargetRestrictRectHeight) / 2F;
                return new RectF(left, top, left + mTargetRestrictRectWidth, top + mTargetRestrictRectHeight);
            default:
                return new RectF();
        }
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
        invalidate();
    }

    public float getTargetRestrictCircleRadius() {
        return mTargetRestrictCircleRadius;
    }

    public void setTargetRestrictCircleRadius(float targetRestrictCircleRadius) {
        mTargetRestrictCircleRadius = targetRestrictCircleRadius;
        invalidate();
    }
}

package cn.yanhu.imchat.view.emojiicon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

public class VerticalImageSpan2 extends ImageSpan {

    private WeakReference<Drawable> mDrawableRef;

    public VerticalImageSpan2(Context context, Bitmap bitmap, int verticalAlignment) {

        super(context, bitmap, verticalAlignment);

    }

    public VerticalImageSpan2(Context context, int resId, int verticalAlignment) {

        super(context, resId, verticalAlignment);

    }

    @Override

    public int getSize(Paint paint, CharSequence text, int start, int end,

                       Paint.FontMetricsInt fontMetricsInt) {

        Drawable drawable = getDrawable();

        Rect rect = drawable.getBounds();

        if (fontMetricsInt != null) {

            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();

            int fontHeight = fmPaint.descent - fmPaint.ascent;

            int drHeight = rect.bottom - rect.top;

            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;

            fontMetricsInt.top = fontMetricsInt.ascent;

            fontMetricsInt.bottom = centerY + drHeight / 2;

            fontMetricsInt.descent = fontMetricsInt.bottom;

        }

        return rect.right;

    }

    @Override

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,

                     int bottom, Paint paint) {

        Drawable drawable = getCachedDrawable();

        canvas.save();

        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();

        int fontHeight = fmPaint.descent - fmPaint.ascent;

        int centerY = y + fmPaint.descent - fontHeight / 2;

        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;

        canvas.translate(x, transY);

        drawable.draw(canvas);

        canvas.restore();

    }

    private Drawable getCachedDrawable() {

        WeakReference<Drawable> wr = mDrawableRef;

        Drawable d = null;

        if (wr != null) {

            d = wr.get();

        }

        if (d == null) {

            d = getDrawable();

            mDrawableRef = new WeakReference<>(d);

        }

        return d;

    }


}


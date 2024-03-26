package cn.yanhu.commonres.view.gridimage;

import android.content.Context;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

/**
 * @author: zhengjun
 * created: 2023/7/14
 * desc:
 */
public class RatioImageView extends GifImageView {

    public RatioImageView(Context context) {
        super(context);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
////        switch (event.getAction()) {
////            case MotionEvent.ACTION_DOWN:
////                Drawable drawable = getDrawable();
////                if (drawable != null) {
////                    drawable.mutate().setColorFilter(Color.GRAY,
////                            PorterDuff.Mode.MULTIPLY);
////                }
////                break;
////            case MotionEvent.ACTION_MOVE:
////                break;
////            case MotionEvent.ACTION_CANCEL:
////            case MotionEvent.ACTION_UP:
////                Drawable drawableUp = getDrawable();
////                if (drawableUp != null) {
////                    drawableUp.mutate().clearColorFilter();
////                }
////                break;
////        }
//
//        return super.onTouchEvent(event);
//    }
}

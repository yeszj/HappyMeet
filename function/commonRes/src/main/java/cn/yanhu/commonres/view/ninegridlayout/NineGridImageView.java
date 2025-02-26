package cn.yanhu.commonres.view.ninegridlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.commonres.R;
import cn.yanhu.commonres.manager.ImageThumbUtils;

@SuppressLint("AppCompatCustomView")
public class NineGridImageView extends AppCompatImageView {

    private static final RequestOptions OPTIONS = new RequestOptions()
            .placeholder(R.drawable.shape_bg_gray)//图片加载出来前，显示的图片
            .fallback(R.drawable.shape_bg_gray) //url为空的时候,显示的图片
            .error(R.drawable.shape_bg_gray);//图片加载失败后，显示的图片


    public NineGridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NineGridImageView(Context context) {
        super(context);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Drawable drawable = getDrawable();
//                if (drawable != null) {
//                    drawable.mutate().setColorFilter(Color.GRAY,
//                            PorterDuff.Mode.MULTIPLY);
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                Drawable drawableUp = getDrawable();
//                if (drawableUp != null) {
//                    drawableUp.mutate().clearColorFilter();
//                }
//                break;
//        }
//
//        return super.onTouchEvent(event);
//    }

    @Override
    public void onAttachedToWindow() {
      //  setImageUrl(url);
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        //setImageBitmap(null);
        super.onDetachedFromWindow();
    }


    public void setImageUrl(String url) {
        String thumbUrl = ImageThumbUtils.getThumbUrl(url);
        if (!TextUtils.isEmpty(thumbUrl)) {
            if ( getContext() != null) {
                int corner = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_6);
                Glide.with(getContext()).load(thumbUrl)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .transform(new CenterCrop(), new RoundedCorners(corner))
                        .apply(OPTIONS)
                        .into(this);
            }
        }
    }

}
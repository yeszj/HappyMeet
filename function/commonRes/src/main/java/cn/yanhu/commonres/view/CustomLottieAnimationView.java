package cn.yanhu.commonres.view;

import android.content.Context;
import android.util.AttributeSet;

import com.airbnb.lottie.LottieAnimationView;

/**
 * @author: zhengjun
 * created: 2025/3/14
 * desc:
 */
public class CustomLottieAnimationView extends LottieAnimationView {
    public CustomLottieAnimationView(Context context) {
        super(context);
        init();
    }

    public CustomLottieAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomLottieAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        this.setFailureListener(result -> {
        });
    }
}

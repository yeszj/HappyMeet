package cn.huanyuan.sweetlove.func.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import cn.huanyuan.sweetlove.R;
import cn.huanyuan.sweetlove.bean.GlobalGiftBean;
import cn.huanyuan.sweetlove.databinding.GlobalGiftLayoutBinding;
import cn.yanhu.baselib.utils.AnimationUtil;


/*
 * 全局礼物飘屏
 * */
public class GlobalGiftLayout extends FrameLayout {
    GlobalGiftLayoutBinding binding;
    private boolean isShowing = false;

    public GlobalGiftLayout(Context context) {
        this(context, null);
    }

    public GlobalGiftLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View mInflater = LayoutInflater.from(context).inflate(R.layout.global_gift_layout, this, false);
        binding = DataBindingUtil.bind(mInflater);
        initView();
    }


    private void initView() {
        this.addView(binding.getRoot());
    }

    public void setModel(GlobalGiftBean model) {
        binding.setBean(model);
    }

    public boolean isShowing() {
        return isShowing;
    }

    public AnimatorSet startAnimation() {
        ObjectAnimator flyFromLtoR = AnimationUtil.createFlyFromLtoR(binding.getRoot(), +getWidth(), 0, 5000, new LinearInterpolator());
        flyFromLtoR.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                GlobalGiftLayout.this.setVisibility(View.VISIBLE);
                GlobalGiftLayout.this.setAlpha(1f);
                isShowing = true;
            }
        });


        ObjectAnimator flyFromLtoR2 = AnimationUtil.createFlyFromLtoR(binding.getRoot(), 0, -getWidth(), 5000, new LinearInterpolator());
        flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                GlobalGiftLayout.this.setVisibility(View.INVISIBLE);
                isShowing = false;
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(flyFromLtoR).before(flyFromLtoR2);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowing = false;
            }
        });
        return animatorSet;
    }
}

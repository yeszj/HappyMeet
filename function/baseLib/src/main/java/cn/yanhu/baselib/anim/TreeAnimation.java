package cn.yanhu.baselib.anim;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import cn.yanhu.baselib.anim.interpolator.SpringInterpolator;


/**
 * @author zhengjun
 * @desc 实现仿蚂蚁森林树木弹动动画
 * @create at 2020-09-03 10:22
 */

public class TreeAnimation {

    public static Animation getAnimation() {
        // 创建缩放的动画对象
        ScaleAnimation sa = new ScaleAnimation(1f, 1.0f, 1.0f, 1.1f, ScaleAnimation.RELATIVE_TO_SELF, 0.0f, ScaleAnimation.RELATIVE_TO_SELF, 1.0f);
        // 设置动画播放的时间
        sa.setDuration(1500);
        sa.setInterpolator(new SpringInterpolator(0.3f));
        return sa;
    }
    public static Animation getAnimation(float toY,float factor) {
        // 创建缩放的动画对象
        ScaleAnimation sa = new ScaleAnimation(1f, 1.0f, 1.0f, toY, ScaleAnimation.RELATIVE_TO_SELF, 0.0f, ScaleAnimation.RELATIVE_TO_SELF, 1.0f);
        // 设置动画播放的时间
        sa.setDuration(1500);
        sa.setInterpolator(new SpringInterpolator(factor));
        return sa;
    }



}

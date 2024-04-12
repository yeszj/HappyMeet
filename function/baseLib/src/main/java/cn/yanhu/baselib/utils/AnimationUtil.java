package cn.yanhu.baselib.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;

public class AnimationUtil {
    /**
     * @param target
     * @param star 动画起始坐标
     * @param end 动画终止坐标
     * @param duration 持续时间
     * @return
     * 创建一个从右到左的飞入动画
     */
    public static ObjectAnimator createFlyFromLtoR(final View target, float star, float end, int duration, TimeInterpolator interpolator) {
//1.个人信息先飞出来
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(target, "translationX",
                star, end);
        anim1.setInterpolator(interpolator);
        anim1.setDuration(duration);
        return anim1;
    }

    /**
     * @return
     * 按顺序播放动画
     */
    public static AnimatorSet startAnimation(ObjectAnimator animator1, ObjectAnimator animator2, ObjectAnimator animator3){
        AnimatorSet animSet = new AnimatorSet();
// animSet.playSequentially(animators);
        animSet.play(animator1).before(animator2);
        animSet.play(animator3).after(animator2);
        animSet.start();
        return animSet;
    }
}

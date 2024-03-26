package cn.yanhu.baselib.anim.interpolator;

import android.view.animation.Interpolator;

/**
 * @author zhengjun
 * @desc
 * @create at 2020-09-03 10:23
 */

public class SpringInterpolator implements Interpolator {
    //控制弹簧系数
    private final float factor;

    public SpringInterpolator(float factor) {
        this.factor = factor;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) -(Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) );
    }
}

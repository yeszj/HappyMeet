package cn.yanhu.baselib.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * @author zhengjun
 * @desc
 * @create at 2021/4/19 13:26
 */

public class ShakeAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "translationY", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
        );
    }
}
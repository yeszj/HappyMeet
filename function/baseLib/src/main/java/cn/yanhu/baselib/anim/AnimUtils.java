package cn.yanhu.baselib.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjun
 * @desc
 * @create at 2021/4/19 13:28
 */

public class AnimUtils {
    private static final long DURATION = BaseViewAnimator.DURATION;
    private static final long NO_DELAY = 0;
    public static final int INFINITE = -1;
    public static final float CENTER_PIVOT = Float.MAX_VALUE;

    private final BaseViewAnimator animator;
    private final long duration;
    private final long delay;
    private final boolean repeat;
    private final int repeatTimes;
    private final int repeatMode;
    private final Interpolator interpolator;
    private final float pivotX, pivotY;
    private final List<Animator.AnimatorListener> callbacks;
    private final View target;

    private AnimUtils(AnimationComposer animationComposer) {
        animator = animationComposer.animator;
        duration = animationComposer.duration;
        delay = animationComposer.delay;
        repeat = animationComposer.repeat;
        repeatTimes = animationComposer.repeatTimes;
        repeatMode = animationComposer.repeatMode;
        interpolator = animationComposer.interpolator;
        pivotX = animationComposer.pivotX;
        pivotY = animationComposer.pivotY;
        callbacks = animationComposer.callbacks;
        target = animationComposer.target;
    }

    public static AnimationComposer with(BaseViewAnimator animator) {
        return new AnimationComposer(animator);
    }

    public interface AnimatorCallback {
        public void call(Animator animator);
    }

     static class EmptyAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    public static final class AnimationComposer {

        private List<Animator.AnimatorListener> callbacks = new ArrayList<>();

        private BaseViewAnimator animator;
        private long duration = DURATION;

        private long delay = NO_DELAY;
        private boolean repeat = false;
        private int repeatTimes = 0;
        private int repeatMode = ValueAnimator.RESTART;
        private float pivotX = AnimUtils.CENTER_PIVOT, pivotY = AnimUtils.CENTER_PIVOT;
        private Interpolator interpolator;
        private View target;

        private AnimationComposer(BaseViewAnimator animator) {
            this.animator = animator;
        }

        public AnimationComposer duration(long duration) {
            this.duration = duration;
            return this;
        }

        public AnimationComposer delay(long delay) {
            this.delay = delay;
            return this;
        }

        public AnimationComposer interpolate(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public AnimationComposer pivot(float pivotX, float pivotY) {
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            return this;
        }

        public AnimationComposer pivotX(float pivotX) {
            this.pivotX = pivotX;
            return this;
        }

        public AnimationComposer pivotY(float pivotY) {
            this.pivotY = pivotY;
            return this;
        }

        public AnimationComposer repeat(int times) {
            if (times < INFINITE) {
                throw new RuntimeException("Can not be less than -1, -1 is infinite loop");
            }
            repeat = times != 0;
            repeatTimes = times;
            return this;
        }

        public AnimationComposer repeatMode(int mode) {
            repeatMode = mode;
            return this;
        }

        public AnimationComposer withListener(Animator.AnimatorListener listener) {
            callbacks.add(listener);
            return this;
        }

        public AnimationComposer onStart(final AnimatorCallback callback) {
            callbacks.add(new EmptyAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    callback.call(animation);
                }
            });
            return this;
        }

        public AnimationComposer onEnd(final AnimatorCallback callback) {
            callbacks.add(new EmptyAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.call(animation);
                }
            });
            return this;
        }

        public AnimationComposer onCancel(final AnimatorCallback callback) {
            callbacks.add(new EmptyAnimatorListener() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    callback.call(animation);
                }
            });
            return this;
        }

        public AnimationComposer onRepeat(final AnimatorCallback callback) {
            callbacks.add(new EmptyAnimatorListener() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    callback.call(animation);
                }
            });
            return this;
        }

        public YoYoString playOn(View target) {
            this.target = target;
            return new YoYoString(new AnimUtils(this).play(), this.target);
        }

    }

    /**
     * YoYo string, you can use this string to control your YoYo.
     */
    public static final class YoYoString {

        private BaseViewAnimator animator;
        private View target;

        private YoYoString(BaseViewAnimator animator, View target) {
            this.target = target;
            this.animator = animator;
        }

        public boolean isStarted() {
            return animator.isStarted();
        }

        public boolean isRunning() {
            return animator.isRunning();
        }

        public void stop() {
            stop(true);
        }

        public void stop(boolean reset) {
            animator.cancel();

            if (reset)
                animator.reset(target);
        }
    }

    private BaseViewAnimator play() {
        animator.setTarget(target);

        if (pivotX == AnimUtils.CENTER_PIVOT) {
            ViewCompat.setPivotX(target, target.getMeasuredWidth() / 2.0f);
        } else {
            target.setPivotX(pivotX);
        }
        if (pivotY == AnimUtils.CENTER_PIVOT) {
            ViewCompat.setPivotY(target, target.getMeasuredHeight() / 2.0f);
        } else {
            target.setPivotY(pivotY);
        }

        animator.setDuration(duration)
                .setRepeatTimes(repeatTimes)
                .setRepeatMode(repeatMode)
                .setInterpolator(interpolator)
                .setStartDelay(delay);

        if (callbacks.size() > 0) {
            for (Animator.AnimatorListener callback : callbacks) {
                animator.addAnimatorListener(callback);
            }
        }
        animator.animate();
        return animator;
    }
}

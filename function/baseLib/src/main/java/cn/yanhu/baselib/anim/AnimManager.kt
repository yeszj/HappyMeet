package cn.yanhu.baselib.anim

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Path
import android.graphics.PathMeasure
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.*
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.yanhu.baselib.anim.interpolator.SpringInterpolator
import cn.yanhu.baselib.callBack.OnAnimCallBack
import cn.yanhu.baselib.callBack.OnAnimaLoadListener
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils


/**
 * @author: witness
 * created: 2022/5/30
 * desc:
 */
object AnimManager {

    fun showLoadingAlphaAnim(view: View,duration: Long = 4000) :ValueAnimator{
        val animator = ValueAnimator.ofFloat(1f, 0.4f,1f)
        animator.duration = duration
        animator.repeatCount = 9999
        animator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Float
            view.alpha = value
        }
        animator.start()
        return animator
    }

    fun showVideoMiniWindowScaleAnim(view: View, listener: OnAnimaLoadListener) {
        val sa = ScaleAnimation(
            1f,
            0.2f,
            1f,
            0.2f,
            ScreenUtils.getAppScreenWidth() * 0.9f,
            ScreenUtils.getAppScreenHeight() * 0.8f
        )
        // 设置动画播放的时间
        // 设置动画播放的时间
        sa.duration = 300
        sa.fillAfter = true
        sa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation) {
            }

            override fun onAnimationEnd(p0: Animation) {
                listener.onAnimEnd(p0)
                ThreadUtils.getMainHandler().postDelayed({
                    sa.fillBefore = true
                    sa.reset()
                    view.clearAnimation()
                }, 500)
            }

            override fun onAnimationRepeat(p0: Animation) {
            }
        })
        view.startAnimation(sa)
    }

    fun showMiniWindowScaleAnim(view: View, listener: OnAnimaLoadListener) {
        val sa = ScaleAnimation(
            1f,
            0.1f,
            1f,
            0.1f,
            ScreenUtils.getAppScreenWidth() * 0.8f,
            ScreenUtils.getAppScreenHeight() * 0.8f
        )
        // 设置动画播放的时间
        // 设置动画播放的时间
        sa.duration = 300
        sa.fillAfter = true
        sa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation) {
            }

            override fun onAnimationEnd(p0: Animation) {
                listener.onAnimEnd(p0)
                ThreadUtils.getMainHandler().postDelayed({
                    sa.fillBefore = true
                    sa.reset()
                    view.clearAnimation()
                }, 500)
            }

            override fun onAnimationRepeat(p0: Animation) {
            }
        })
        view.startAnimation(sa)
    }

    fun setScaleHideAnim(view: View) {
        // 创建缩放的动画对象
        val sa = AlphaAnimation(1f, 0f)
        // 设置动画播放的时间
        // 设置动画播放的时间
        sa.duration = 1000
        sa.interpolator = AccelerateDecelerateInterpolator()
        view.startAnimation(sa)
        sa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
    }

    @JvmStatic
    fun showMarginTopAnimator(view: View, start: Int, end: Int, duration: Long = 200) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = duration
        animator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams as MarginLayoutParams
            layoutParams.topMargin = value
            view.layoutParams = layoutParams
        }
        animator.start()
    }

    fun showMarginLeftAnimator(view: View, start: Int, end: Int, duration: Long = 200) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = duration
        animator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams as MarginLayoutParams
            layoutParams.leftMargin = value
            view.layoutParams = layoutParams
        }
        animator.start()
    }

    fun createDropAnimator(view: View, start: Int, end: Int, duration: Long = 200, onAnimCallBack: OnAnimCallBack?=null, isReSet:Boolean = false) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = duration
        animator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        animator.addListener(object : AnimatorListener{
            override fun onAnimationStart(animation: Animator) {
            }
            override fun onAnimationEnd(animation: Animator) {
                if (isReSet){
                    val layoutParams = view.layoutParams
                    layoutParams.height = start
                    view.layoutParams = layoutParams
                }
                onAnimCallBack?.onAnimEnd()
            }
            override fun onAnimationCancel(animation: Animator) {
            }
            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        animator.start()
    }


    //显示按钮放大缩小动画
    fun showScaleAnim(animView: View): AnimatorSet {
        val objectAnimator = ObjectAnimator.ofFloat(animView, "scaleX", 1f, 1.15f, 1f)
        val objectAnimator2 = ObjectAnimator.ofFloat(animView, "scaleY", 1f, 1.15f, 1f)
        objectAnimator.repeatCount = ValueAnimator.INFINITE
        objectAnimator2.repeatCount = ValueAnimator.INFINITE
        val animatorSet = AnimatorSet()
        animatorSet.duration = 1000
        animatorSet.playTogether(objectAnimator, objectAnimator2)
        if (!animatorSet.isRunning) {
            animatorSet.start()
        }
        return animatorSet
    }

    fun showOneScaleView(animView: View?, duration: Int): AnimatorSet {
        val objectAnimator = ObjectAnimator.ofFloat(animView, "scaleX", 1f, 1.2f, 1f)
        val objectAnimator2 = ObjectAnimator.ofFloat(animView, "scaleY", 1f, 1.2f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.duration = duration.toLong()
        animatorSet.playTogether(objectAnimator, objectAnimator2)
        if (!animatorSet.isRunning) {
            animatorSet.start()
        }
        return animatorSet
    }

    fun showOneScaleView(animView: View?, duration: Int,scaleValue:Float = 1.2f): AnimatorSet {
        val objectAnimator = ObjectAnimator.ofFloat(animView, "scaleX", 1f, scaleValue, 1f)
        val objectAnimator2 = ObjectAnimator.ofFloat(animView, "scaleY", 1f, scaleValue, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.duration = duration.toLong()
        animatorSet.playTogether(objectAnimator, objectAnimator2)
        if (!animatorSet.isRunning) {
            animatorSet.start()
        }
        return animatorSet
    }

    //显示图片360度旋转动画
    fun showRotateAnim(imageView: ImageView): ObjectAnimator {
        val rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f)
        rotateAnimator.interpolator = LinearInterpolator()
        rotateAnimator.repeatCount = ValueAnimator.INFINITE
        rotateAnimator.duration = 5000
        rotateAnimator.start()
        return rotateAnimator
    }
    interface OnAnimatorListener {
        fun onAnimationEnd(animator: Animator?)
    }
    fun doCartAnimator(
        activity: Activity, startView: ImageView,
        cartView: View, parentView: ViewGroup,
        listener: AnimManager.OnAnimatorListener?,
    ) {
        //第一步：
        //创造出执行动画的主题---imageView
        //代码new一个imageView，图片资源是上面的imageView的图片
        // (这个图片就是执行动画的图片，从开始位置出发，经过一个抛物线（贝塞尔曲线），移动到目标位置)
        val goods = ImageView(activity)
        goods.setPadding(1, 1, 1, 1)
        //图片切割方式
        goods.scaleType = ImageView.ScaleType.CENTER_CROP
        //获取图片资源
        goods.setImageDrawable(startView.drawable)
        //设置RelativeLayout容器(这里必须设置RelativeLayout 设置LinearLayout动画会失效)
        val layoutParams = startView.layoutParams
        val params = RelativeLayout.LayoutParams(layoutParams.width, layoutParams.height)
        //把动画view添加到动画层
        parentView.addView(goods, params)

        //第二步:
        //得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        val parentLocation = IntArray(2)
        //获取购买按钮的在屏幕的X、Y坐标（这也是动画开始的坐标）
        parentView.getLocationInWindow(parentLocation)
        val startLoc = IntArray(2)
        //获取开始图片在屏幕中的位置
        startView.getLocationInWindow(startLoc)
        //得到目标图片的坐标(用于计算动画结束后的坐标)
        val endLoc = IntArray(2)
        cartView.getLocationInWindow(endLoc)

        //第三步:
        //正式开始计算动画开始/结束的坐标
        //开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        val startX = (startLoc[0] - parentLocation[0] + startView.width / 2).toFloat() // 动画开始的X坐标
        val startY = (startLoc[1] - parentLocation[1] + startView.height / 2).toFloat() //动画开始的Y坐标

        //商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        val toX = (endLoc[0] - parentLocation[0] + cartView.width / 5).toFloat()
        val toY = (endLoc[1] - parentLocation[1]).toFloat()
        if (toY < 0) {
            parentView.removeView(goods)
            return
        }
        //第四步:
        //计算中间动画的插值坐标，绘制贝塞尔曲线
        val path = Path()
        //移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY)
        //第一个起始坐标越大，贝塞尔曲线的横向距离就会越大 toX,toY:为终点
        path.quadTo((startX + toX) / 2, startY, toX, toY)
        val pathMeasure = PathMeasure(path, false)
        //实现动画具体博客可参考 鸿洋大神的https://blog.csdn.net/lmj623565791/article/details/38067475
        val valueAnimator = ValueAnimator.ofFloat(0f, pathMeasure.length)
        //设置动画时间
        valueAnimator.duration = 300
        //LinearInterpolator补间器:它的主要作用是可以控制动画的变化速率，比如去实现一种非线性运动的动画效果
        //具体可参考郭霖大神的：https://blog.csdn.net/guolin_blog/article/details/44171115
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            //更新动画
            val value = animation.animatedValue as Float
            val currentPosition = FloatArray(2)
            pathMeasure.getPosTan(value, currentPosition, null)
            goods.translationX = currentPosition[0] //改变了ImageView的X位置
            goods.translationY = currentPosition[1] //改变了ImageView的Y位置
        }

        //第五步:
        //开始执行动画
        valueAnimator.start()

        //第六步:
        //对动画添加监听
        valueAnimator.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                //onAnimationStart()方法会在动画开始的时候调用
            }

            //onAnimationEnd()方法会在动画结束的时候调用
            override fun onAnimationEnd(animation: Animator) {
                //把移动的图片imageView从父布局里移除
                parentView.removeView(goods)
                listener?.onAnimationEnd(animation)
            }

            override fun onAnimationCancel(animation: Animator) {
                //onAnimationCancel()方法会在动画被取消的时候调用
            }

            override fun onAnimationRepeat(animation: Animator) {
                //onAnimationRepeat()方法会在动画重复执行的时候调用
            }
        })
    }


    fun showRewardScaleAnim(animView: View): ScaleAnimation {
        // 创建缩放的动画对象
        val scaleAnimation = ScaleAnimation(
            0.0f,
            1f,
            0.0f,
            1f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnimation.duration = 500
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                showGiftShadeAnim(animView)
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        animView.startAnimation(scaleAnimation)
        return scaleAnimation
    }


    fun showRewardScaleHideAnim(animView: View): ScaleAnimation {
        // 创建缩放的动画对象
        val scaleAnimation = ScaleAnimation(
            1f,
            0f,
            1f,
            0f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnimation.duration = 200
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                animView.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        animView.startAnimation(scaleAnimation)
        return scaleAnimation
    }


    fun showGiftShadeAnim(animView: View): ScaleAnimation {
        // 创建缩放的动画对象
        val scaleAnimation = ScaleAnimation(
            1.0f,
            1.15f,
            1.0f,
            1.15f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        // 设置动画播放的时间
        scaleAnimation.duration = 1000
        scaleAnimation.interpolator = SpringInterpolator(0.3f)
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                //showGiftShadeAnim(animView)
                ThreadUtils.getMainHandler().postDelayed({
                    showRewardScaleHideAnim(animView)
                },1000)

            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        animView.startAnimation(scaleAnimation)
        return scaleAnimation
    }


    fun removeAnimSet(animatorSet: Animator?) {
        animatorSet?.apply {
            removeAllListeners()
            if (animatorSet is ValueAnimator){
                animatorSet.removeAllUpdateListeners()
            }
            pause()
            cancel()
        }
    }

    fun scrollToPosition(
        x: Int,
        y: Int,
        scrollView: View?,
        animatorListener: AnimatorListener?,
        duration: Long
    ) {
        val xTranslate = ObjectAnimator.ofInt(scrollView, "scrollX", x)
        val yTranslate = ObjectAnimator.ofInt(scrollView, "scrollY", y)
        val animators = AnimatorSet()
        animators.duration = duration
        animators.playTogether(xTranslate, yTranslate)
        animators.start()
        if (animatorListener != null) animators.addListener(animatorListener)
    }

}
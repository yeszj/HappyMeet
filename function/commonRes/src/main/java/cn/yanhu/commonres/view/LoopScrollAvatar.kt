package cn.yanhu.commonres.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.CommonUtils
import com.zj.dimens.R
import java.lang.ref.WeakReference

/**
 * @author: zhengjun
 * created: 2024/9/12
 * desc:
 */
const val START_AVATAR_LOOP = 111

class LoopScrollAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    //动画播放时长
    private val animDuration = 700L

    //动画间隔播放时间
    private val animIntervalTime = 1000L

    //两边头像的缩放程度
    private val scaleFrom = 0.7F

    //头像大小
    private val avatarSize = CommonUtils.getDimension(R.dimen.dp_40)

    //从当前位置滚动到下一位置需要移动的距离
    private var scrollLength = 0F

    //下次要显示的图片角标
    private var index = 0

    var imgUrlList = mutableListOf<String>()


    //缓存复用ImageView
    private val ivCache = mutableListOf(
        createImageView(),
        createImageView(),
        createImageView(),
        createImageView()
    )
    private val handler by lazy {
        LoopHandler(this)
    }

    fun initImageView(){
        //前三位的头像先addView显示出来
        //放最右
       // addImageView(ALIGN_PARENT_RIGHT)
        removeAllViews()
        if (imgUrlList.isEmpty()){
            return
        }
        if (imgUrlList.size>1){
            //放中间
            addImageView(CENTER_HORIZONTAL)
        }
        //默认放左边
        addImageView()
        if (imgUrlList.size>1){
            startLoop()
        }
    }

    class LoopHandler() : Handler() {
        private var lWeak: WeakReference<LoopScrollAvatar>? = null

        constructor(loopScrollAvatar: LoopScrollAvatar) : this() {
            lWeak = WeakReference(loopScrollAvatar)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            lWeak?.get()?.apply {
                if ((context as? Activity)?.isDestroyed == false) {
                    startAnimMove()
                    sendLoopMsg()
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scrollLength = (width - avatarSize) / 2F
    }

    /**
     * 创建圆形头像ImageView
     */
    private fun createImageView(): ImageView {
        return AppCompatImageView(context)
    }

    /**
     * 摆放头像ImageView
     */
    private fun addImageView(rule: Int = ALIGN_PARENT_LEFT) {
        //复用缓存
        val iv = if (ivCache.isNotEmpty()) {
            ivCache[0]
        } else {
            createImageView()
        }
        //当前已在屏幕显示的控件不要复用，防止params混乱
        ivCache.remove(iv)
        if (index>imgUrlList.size){
            return
        }
        CoilImgUtils.loadCircleImg(imgUrlList[index], iv, borderWidth = CommonUtils.getDimension(R.dimen.dp_1).toFloat())
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        if (rule != ALIGN_PARENT_LEFT) {
            iv.scaleX = 0.8f
            iv.scaleY = 0.8f
            iv.alpha = 0.6f
        }
        //图片资源全部播放完之后要从头重播
        index = (index + 1) % imgUrlList.size

        //设置在RelativeLayout中的显示位置
        val lp = LayoutParams(avatarSize, avatarSize)
        lp.addRule(rule)
        iv.layoutParams = lp

        addView(iv)
    }

    /**
     * 轮播滚动动效
     */
    private fun startAnimMove() {
        //添加一个即将从左边移进屏幕的ImageView
        addImageView()
        //上行代码刚添加进来的最左边头像（此时RelativeLayout的mChildrenCount=4）
        getChildAt(2)?.apply {
            //设置起始的低透明度 和 小size
            alpha = 0.6F
            scaleX = scaleFrom
            scaleY = scaleFrom
            //先设置左边部分向左移出控件，即挡住左边不显示（然后才能translationXBy移进屏幕）
            translationX = -scrollLength

            //translationXBy指的是从当前位置开始移动多少距离（区别于translationX）
            //alpha是从当前透明度（即0.6F）变为设置的透明度（即1F）
            //scaleX是从当前宽度比例（即scaleFrom）变为设置的宽度比例（即1F）
            animate().translationXBy(scrollLength).alpha(1F).scaleX(1F).scaleY(1F)
                .setDuration(animDuration).start()
        }
        //中间俩头像只需设置平移的距离即可
        getChildAt(1)?.apply {
            animate().translationXBy(scrollLength).alpha(0.6F).scaleX(scaleFrom).scaleY(scaleFrom).setDuration(animDuration).start()
        }
        //设置平移的距离
//        getChildAt(2)?.apply {
//            animate().translationXBy(scrollLength).setDuration(animDuration).start()
//        }
        //最右边的头像（从完整显示 到 透明度和大小都变小，并且右移出屏幕）（因为是最先add进来的View，所以index=0）
        getChildAt(0)?.let { iv ->
            iv.animate().translationXBy(scrollLength).alpha(0F).scaleX(scaleFrom).scaleY(scaleFrom)
                .setDuration(animDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        //清除ImageView已有属性，并添加进ivCache缓存
                        iv.animate().setListener(null)
                        iv.clearAnimation()
                        iv.translationX = 0F
                        iv.scaleX = 1.0F
                        iv.scaleY = 1.0F
                        iv.alpha = 1F
                        //从RelativeLayout移出
                        removeView(iv)
                        ivCache.add(iv as ImageView)
                    }
                }).start()
        }
    }

    private fun sendLoopMsg() {
        handler.sendEmptyMessageDelayed(START_AVATAR_LOOP, animIntervalTime + animDuration)
    }

     var looping = false

    /**
     * 开始轮播
     */
    fun startLoop() {
        if (looping) {
            return
        }
        looping = true
        sendLoopMsg()
    }

    /**
     * 停止轮播
     */
    fun stopLoop() {
        looping = false
        handler.removeCallbacksAndMessages(null)
    }
}
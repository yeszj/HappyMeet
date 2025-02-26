package cn.huanyuan.sweetlove.func.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.UserOnlineAnimationItemBinding
import cn.yanhu.baselib.utils.AnimationUtil
import cn.yanhu.commonres.bean.BaseUserInfo

/*
 * 全局礼物飘屏
 * */
class GlobalUserOnlineLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : FrameLayout(
    context!!, attrs
) {
    var binding: UserOnlineAnimationItemBinding?
    var isShowing = false
        private set

    init {
        val mInflater =
            LayoutInflater.from(context).inflate(R.layout.user_online_animation_item, this, false)
        binding = DataBindingUtil.bind(mInflater)
        isClickable = false
        isFocusable = false
        initView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    private fun initView() {
        this.addView(binding!!.root)
    }

    fun setUserInfo(userInfo: BaseUserInfo) {
        binding?.apply {
            avatarView.setUserAvatar(userInfo)
            val level = userInfo.level
            userLevel.setUserLevel(level)
            //val userEnterBg = LevelTagConfig.getUserEnterBg(level)
           // ivBg.setImageResource(userEnterBg)
            tvUserName.text = userInfo.nickName
        }
    }
    fun startAnimation(): AnimatorSet {
        val flyFromLtoR =
            AnimationUtil.createFlyFromLtoR(this, +width.toFloat(), 0f, 5000, LinearInterpolator())
        flyFromLtoR.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                this@GlobalUserOnlineLayout.visibility = VISIBLE
                this@GlobalUserOnlineLayout.alpha = 1f
                isShowing = true
            }
        })
        val flyFromLtoR2 =
            AnimationUtil.createFlyFromLtoR(this, 0f, -width.toFloat(), 5000, LinearInterpolator())
        flyFromLtoR2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@GlobalUserOnlineLayout.visibility = INVISIBLE
                isShowing = false
            }
        })
        val animatorSet = AnimatorSet()
        animatorSet.play(flyFromLtoR).before(flyFromLtoR2)
        animatorSet.start()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isShowing = false
            }
        })
        return animatorSet
    }
}
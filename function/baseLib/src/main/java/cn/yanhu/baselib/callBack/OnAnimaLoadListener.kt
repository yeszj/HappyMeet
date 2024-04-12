package cn.yanhu.baselib.callBack

import android.view.animation.Animation

/**
 * @author: zhengjun
 * created: 2023/4/18
 * desc:
 */
interface OnAnimaLoadListener {
    fun onAnimEnd(animation: Animation)
    fun onAnimationStart(){}
}
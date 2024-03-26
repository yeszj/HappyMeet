package cn.yanhu.baselib.utils.ext

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import cn.yanhu.baselib.callBack.OnSingleClickListener

fun View.visible() {
    if (this.visibility != View.VISIBLE) this.visibility = View.VISIBLE
}

fun View.gone() {
    if (this.visibility != View.GONE) this.visibility = View.GONE
}

fun View.invisible() {
    if (this.visibility != View.INVISIBLE) this.visibility = View.INVISIBLE
}

@SuppressLint("CheckResult")
fun View.setOnSingleClickListener(timeSecond: Long = 1, singleEvent: (View) -> Any?) {
    setOnClickListener {
        if (System.currentTimeMillis() - clickTime > timeSecond * 500L) {
            singleEvent.invoke(this)
            clickTime = System.currentTimeMillis()
        }
    }
}

fun View.setClickAlphaListener(timeSecond: Long = 1, singleEvent: (View) -> Any?) {
    setClickAlpha(this)
    setOnClickListener {
        if (System.currentTimeMillis() - clickTime > timeSecond * 500L) {
            singleEvent.invoke(this)
            clickTime = System.currentTimeMillis()
        }
    }
}


fun setClickAlpha(view: View?) {
    view?.setOnTouchListener(object : OnTouchListener {
        var cancelled = false
        val rect = Rect()

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.alpha = 0.6f
                MotionEvent.ACTION_MOVE -> {
                    if (rect.isEmpty) {
                        v.getDrawingRect(rect)
                    }
                    if (!rect.contains(event.x.toInt(), event.y.toInt())) {
                        view.alpha = 1f
                        cancelled = true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!cancelled) {
                        view.alpha = 1f
                    } else {
                        cancelled = false
                    }
                }
            }
            return false
        }
    })
}

/**
 * 防止重复点击
 */
private var clickTime: Long = 0

fun View?.setOnSingleClickListener(listener: OnSingleClickListener, timeSecond: Long = 500) {
    if (this == null) return
    setOnClickListener {
        if (System.currentTimeMillis() - clickTime > timeSecond) {
            clickTime = System.currentTimeMillis()
            listener.onSingleClick(this)
        }
    }
}


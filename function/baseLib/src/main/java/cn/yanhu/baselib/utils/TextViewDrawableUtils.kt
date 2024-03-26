package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.*
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.lang.Exception
import java.lang.Math.abs

/**
 * @author zhengjun
 * @desc
 * @create at 2020/11/16 10:11
 */
object TextViewDrawableUtils {
    @JvmStatic
    fun setDrawableRight(context: Context?, textView: TextView, drawableId: Int) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, ContextCompat.getDrawable(
                context!!, drawableId
            ), null
        )
    }

    @JvmStatic
    fun setDrawableLeft(context: Context?, textView: TextView, drawableId: Int) {
        context?.apply {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    context!!, drawableId
                ), null, null, null
            )
        }

    }


    @JvmStatic
    fun setDrawableLeft(textView: TextView, drawable: Drawable?) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    }

    @JvmStatic
    fun setDrawableRight(textView: TextView, drawable: Drawable?) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
    }

    @JvmStatic
    //设置图片着色器 tint
    fun setDrawableTint(context: Context?, drawableId: Int, color: Int, imageView: ImageView) {
        try {
            val up = ContextCompat.getDrawable(context!!, drawableId)
            val drawableUp = DrawableCompat.wrap(up!!)
            DrawableCompat.setTint(drawableUp, color)
            imageView.setImageDrawable(drawableUp)
            val up1 = ContextCompat.getDrawable(context, drawableId)
            val drawableUp1 = DrawableCompat.unwrap<Drawable>(
                up1!!
            )
            DrawableCompat.setTintList(drawableUp1, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setDrawableLeftTint(context: Context?, drawableId: Int, color: Int, imageView: TextView) {
        try {
            val up = ContextCompat.getDrawable(context!!, drawableId)
            val drawableUp = DrawableCompat.wrap(up!!)
            DrawableCompat.setTint(drawableUp, color)
            TextViewDrawableUtils.setDrawableLeft(imageView,drawableUp)
            val up1 = ContextCompat.getDrawable(context, drawableId)
            val drawableUp1 = DrawableCompat.unwrap<Drawable>(
                up1!!
            )
            DrawableCompat.setTintList(drawableUp1, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 设置触摸事件，让TextView响应ClickableSpan。此处代码参考LinkMovementMethod.onTouchEvent方法
     * 直接给TextView设置LinkMovementMethod后，文本过长时可以滑动，与ellipsize冲突
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setTouchListener(textView: TextView) {
        textView.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val spanned = textView.text as? Spanned ?: return false
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        return true//返回true,接受后续的UP事件
                    }
                    MotionEvent.ACTION_UP -> {
                        var x = event.x.toInt()
                        var y = event.y.toInt()
                        //判断一下按下和抬起的位置，相差太大就不处理
                        if (abs(downX - x) > 8 || abs(downY - y) > 8) {
                            return false
                        }
                        //下面代码都是照搬LinkMovementMethod.onTouchEvent
                        x -= textView.totalPaddingLeft
                        y -= textView.totalPaddingTop

                        x += textView.scrollX
                        y += textView.scrollY

                        val layout: Layout = textView.layout
                        val line = layout.getLineForVertical(y)
                        val off = layout.getOffsetForHorizontal(line, x.toFloat())

                        val links: Array<ClickableSpan> =
                            spanned.getSpans(off, off, ClickableSpan::class.java)

                        if (links.isNotEmpty()) {
                            links[0].onClick(textView)
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

}
package cn.yanhu.commonres.view.roseAnim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.Keep


/**
 * @author: zhengjun
 * created: 2025/2/10
 * desc:
 */
@Keep
class Rose(private val bitmap: Bitmap, val screenWidth: Int) {
    var x = 0f
    var y:Float = 0f // 玫瑰的位置

    var rotation = 0f // 旋转角度

    var scale = 2f // 缩放比例

    var alpha = 0f // 透明度

    var rotationSpeed = 0f // 旋转速度

    var scaleSpeed = 0f // 缩放速度

    var alphaSpeed = 0f // 透明度变化速度

    var fallSpeed = 0f // 下落速度


    init {
        reset()
    }

    fun reset() {
        x = (Math.random() * screenWidth).toFloat() // 随机初始位置
        y = 0f // 从顶部开始
        rotation = 0f
        scale = 1f
        alpha = 1.0f
        rotationSpeed = (Math.random() * 4 - 2).toFloat() // 随机旋转速度
        //scaleSpeed = (Math.random() * 0.01f).toFloat() // 随机缩放速度
        alphaSpeed = (Math.random() * 0.01f).toFloat() // 随机透明度变化速度
        fallSpeed = (Math.random() * 3 + 3).toFloat() // 随机下落速度
    }
    fun update() {
        y += fallSpeed // 更新位置
        rotation += rotationSpeed // 更新旋转角度
        scale += scaleSpeed // 更新缩放比例
        //alpha -= alphaSpeed; // 更新透明度
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.alpha = (alpha * 255).toInt()
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation, bitmap.width / 2f, bitmap.height / 2f)
        canvas.scale(scale, scale)
        canvas.drawBitmap(bitmap, (-bitmap.width / 2).toFloat(), -bitmap.height.toFloat(), paint)
        canvas.restore()
    }

    fun isOffScreen(screenHeight: Int): Boolean {
        return y > screenHeight || alpha <= 0
    }
}
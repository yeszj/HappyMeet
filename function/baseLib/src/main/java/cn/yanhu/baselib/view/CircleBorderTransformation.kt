package cn.yanhu.baselib.view

/**
 * @author: zhengjun
 * created: 2026/1/20
 * desc:
 */
import android.graphics.*
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.min

class CircleBorderTransformation(
    private val borderWidthPx: Float,
    private val borderColor: Int = Color.WHITE
) : Transformation {

    // 注意：Paint 对象不应作为类字段复用！
    // 每次transform调用都应该创建新的Paint实例

    override val cacheKey: String =
        "${javaClass.name}-$borderWidthPx-${borderColor.toHexString()}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // 1. 验证输入Bitmap有效性
        if (input.isRecycled) {
            throw IllegalStateException("Input bitmap is recycled")
        }

        if (input.width == 0 || input.height == 0) {
            return input // 返回原始bitmap
        }

        val diameter = min(input.width, input.height)

        // 2. 创建输出Bitmap（必须使用ARGB_8888）
        val output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)

        // 重要：复制density，避免缩放问题
        output.density = input.density

        val canvas = Canvas(output)

        // 3. 为每次调用创建独立的Paint实例
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
        }

        // 4. 计算正确的裁切区域
        val scale: Float
        val dx: Float
        val dy: Float

        if (input.width > input.height) {
            // 宽图，裁切左右
            scale = diameter.toFloat() / input.height
            dx = (diameter - input.width * scale) / 2f
            dy = 0f
        } else if (input.height > input.width) {
            // 长图，裁切上下
            scale = diameter.toFloat() / input.width
            dx = 0f
            dy = (diameter - input.height * scale) / 2f
        } else {
            // 正方形图
            scale = 1f
            dx = 0f
            dy = 0f
        }

        // 5. 创建并设置Shader
        val shader = BitmapShader(input, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val matrix = Matrix()

        // 先缩放，后平移
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        shader.setLocalMatrix(matrix)

        circlePaint.shader = shader

        // 6. 绘制圆形图片
        val halfBorder = borderWidthPx / 2
        val circleRect = RectF(
            halfBorder,
            halfBorder,
            diameter - halfBorder,
            diameter - halfBorder
        )

        canvas.drawOval(circleRect, circlePaint)

        // 7. 绘制边框（使用新的Paint实例）
        if (borderWidthPx > 0) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = borderWidthPx
                color = borderColor
                isAntiAlias = true
                isDither = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

            canvas.drawOval(circleRect, borderPaint)
        }

        return output
    }

    private fun Int.toHexString() = String.format("#%08X", this)
}
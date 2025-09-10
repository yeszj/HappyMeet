package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.yanhu.commonres.R
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import kotlin.math.max
import androidx.core.view.isGone
import kotlin.math.min

/**
 * @author: zhengjun
 * created: 2025/9/3
 * desc:
 */


/**
 * 改进版的重叠流式布局
 * 修复了测量和布局中的问题
 */
class OverlapFlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // 重叠像素数
    var overlapPx = 20

    // 每行最大数量
    var maxPerLine = 5

    // 头像尺寸
    var avatarSize = 100

    // 存储每行的信息
    private val lineHeights = mutableListOf<Int>()
    private val lineChildCount = mutableListOf<Int>()

    init {
        clipChildren = false
        clipToPadding = false

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.OverlapFlowLayout,
            defStyleAttr,
            0
        ).apply {
            try {
                maxPerLine = getInt(R.styleable.OverlapFlowLayout_maxPerLine, maxPerLine)
                overlapPx =
                    getDimensionPixelSize(R.styleable.OverlapFlowLayout_overlapWidth, overlapPx)
                avatarSize =
                    getDimensionPixelSize(R.styleable.OverlapFlowLayout_overAvatarSize, avatarSize)
            } finally {
                recycle()
            }
        }
    }

    // 存储布局信息
    private val lineInfoList = mutableListOf<LineInfo>()

    data class LineInfo(
        val childCount: Int,
        val lineWidth: Int,
        val lineHeight: Int
    )

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        lineInfoList.clear()

        var totalWidth = paddingLeft + paddingRight
        var totalHeight = paddingTop + paddingBottom
        var currentLineWidth = paddingLeft + paddingRight
        var currentLineHeight = 0
        var currentLineChildCount = 0

        val childSpec = MeasureSpec.makeMeasureSpec(avatarSize, MeasureSpec.EXACTLY)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue

            measureChild(child, childSpec, childSpec)

            val childEffectiveWidth = if (i == 0) {
                child.measuredWidth
            } else {
                child.measuredWidth - overlapPx
            }

            // 检查是否需要换行
            val exceedsMaxPerLine = currentLineChildCount >= maxPerLine
            val exceedsWidth = widthMode != MeasureSpec.UNSPECIFIED &&
                    currentLineWidth + childEffectiveWidth > widthSize

            if ((exceedsMaxPerLine || exceedsWidth) && currentLineChildCount > 0) {
                // 保存当前行信息
                lineInfoList.add(
                    LineInfo(
                        currentLineChildCount,
                        currentLineWidth,
                        currentLineHeight
                    )
                )

                totalHeight += currentLineHeight
                totalWidth = max(totalWidth, currentLineWidth)

                // 重置为新行
                currentLineWidth = paddingLeft + paddingRight + child.measuredWidth
                currentLineHeight = child.measuredHeight
                currentLineChildCount = 1
            } else {
                // 继续当前行
                currentLineWidth += if (i == 0) {
                    child.measuredWidth
                } else {
                    child.measuredWidth - overlapPx
                }
                currentLineHeight = max(currentLineHeight, child.measuredHeight)
                currentLineChildCount++
            }
        }

        // 添加最后一行
        if (currentLineChildCount > 0) {
            lineInfoList.add(LineInfo(currentLineChildCount, currentLineWidth, currentLineHeight))
            totalHeight += currentLineHeight
            totalWidth = max(totalWidth, currentLineWidth)
        }

        // 考虑测量模式
        val measuredWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(totalWidth, widthSize)
            else -> totalWidth
        }

        val measuredHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(totalHeight, heightSize)
            else -> totalHeight
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentTop = paddingTop
        var childIndex = 0

        for (lineInfo in lineInfoList) {
            var currentLeft = paddingLeft
            val lineHeight = lineInfo.lineHeight

            for (i in 0 until lineInfo.childCount) {
                if (childIndex >= childCount) break

                val child = getChildAt(childIndex)
                if (child.isGone) {
                    childIndex++
                    continue
                }

                val childTop = currentTop + (lineHeight - child.measuredHeight) / 2

                child.layout(
                    currentLeft,
                    childTop,
                    currentLeft + child.measuredWidth,
                    childTop + child.measuredHeight
                )

                // 设置z-index确保后面的视图显示在前面
                child.translationZ = childIndex.toFloat()

                // 更新下一个子视图的位置
                currentLeft += child.measuredWidth - overlapPx
                childIndex++
            }

            currentTop += lineHeight
        }
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        // 后面的头像绘制在上面
        return i
    }

    /**
     * 设置头像URL列表
     */
    fun setAvatarUrls(urls: List<String>,strokeColor:Int=0,strokeWidth:Int=0) {
        removeAllViews()
        for (url in urls) {
            addAvatar(url,strokeColor,strokeWidth)
        }
    }

    /**
     * 添加单个头像
     */
    fun addAvatar(url: String,strokeColor:Int,strokeWidth:Int) {
        val imageView = createAvatarImageView(strokeColor,strokeWidth)
        Glide.with(this)
            .load(url)
            .circleCrop()
            .into(imageView)
        addView(imageView)
        requestLayout()
    }

    private fun createAvatarImageView(strokeColor:Int,strokeWidth:Int): RoundedImageView {
        return RoundedImageView(context).apply {
            layoutParams = LayoutParams(avatarSize, avatarSize)
            setOval(true)
            if (strokeWidth>0){
                borderColor = strokeColor
                setBorderWidth(strokeWidth)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    /**
     * 清除所有头像
     */
    fun clearAvatars() {
        removeAllViews()
        requestLayout()
    }
}
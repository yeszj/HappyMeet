package cn.yanhu.baselib.view

import android.content.Context
import android.util.AttributeSet
import cn.yanhu.baselib.R

/**
 * @author: zhengjun
 * created: 2023/4/23
 * desc:边框颜色渐变view
 */
class BorderGradientView : CustomFontTextView {
    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val attrArray = context.obtainStyledAttributes(attrs, R.styleable.BorderGradientView)
        val borderBgColor = attrArray.getColor(R.styleable.BorderGradientView_borderBgColor, 0)
        val borderStartColor =
            attrArray.getColor(R.styleable.BorderGradientView_borderStartColor, 0)
        val borderEndColor = attrArray.getColor(R.styleable.BorderGradientView_borderEndColor, 0)
        val borderRadius = attrArray.getDimension(R.styleable.BorderGradientView_borderRadius, 0f)
        val borderBottomLeftRadius =
            attrArray.getDimension(R.styleable.BorderGradientView_borderBottomLeftRadius, 0f)
        val borderBottomRightRadius =
            attrArray.getDimension(R.styleable.BorderGradientView_borderBottomRightRadius, 0f)
        val borderTopLeftRadius =
            attrArray.getDimension(R.styleable.BorderGradientView_borderTopLeftRadius, 0f)
        val borderTopRightRadius =
            attrArray.getDimension(R.styleable.BorderGradientView_borderTopRightRadius, 0f)
        val gradientType = attrArray.getInt(
            R.styleable.BorderGradientView_borderGradientType, BorderGradientDrawable.LEFT_TO_RIGHT
        )

        val borderWidth =
            attrArray.getDimension(R.styleable.BorderGradientView_borderViewWidth, 0f).toInt()

        attrArray.recycle()

        val colors = intArrayOf(borderStartColor, borderEndColor)
        //初始化并设置四个边框的宽度
        val borderDrawable =
            BorderGradientDrawable(borderWidth, borderWidth, borderWidth, borderWidth)

        borderDrawable.setGradientStyle(gradientType)
        if (borderBgColor!=0){
            borderDrawable.setColor(borderBgColor)
        }
        //设置渐变色
        borderDrawable.setColors(colors)
        //设置圆角大小
        if (borderRadius > 0) {
            borderDrawable.setCornerRadius(borderRadius)
        } else {
            /*圆角的半径，依次为左上角xy半径，右上角xy半径，右下角xy半径，左下角xy半径*/
            val radius = floatArrayOf(
                borderTopLeftRadius,
                borderTopLeftRadius,
                borderTopRightRadius,
                borderTopRightRadius,
                borderBottomRightRadius,
                borderBottomRightRadius,
                borderBottomLeftRadius,
                borderBottomLeftRadius
            )
            borderDrawable.setCornerRadii(radius)
        }
        this.background = borderDrawable
    }

    fun setBorderRadius(borderTopLeftRadius:Int,borderTopRightRadius:Int,borderBottomLeftRadius:Int,borderBottomRightRadius:Int){
        val radius = floatArrayOf(
            borderTopLeftRadius.toFloat(),
            borderTopLeftRadius.toFloat(),
            borderTopRightRadius.toFloat(),
            borderTopRightRadius.toFloat(),
            borderBottomRightRadius.toFloat(),
            borderBottomRightRadius.toFloat(),
            borderBottomLeftRadius.toFloat(),
            borderBottomLeftRadius.toFloat()
        )
        val borderDrawable = this.background
        if (borderDrawable is BorderGradientDrawable){
            borderDrawable.setCornerRadii(radius)
            this.background = borderDrawable
        }
    }

    fun setBorderStrokeColor(colors: MutableList<Int>){
        val borderDrawable = this.background
        if (borderDrawable is BorderGradientDrawable){
            //设置渐变色
            borderDrawable.setColors(colors.toIntArray())
            this.background = borderDrawable
        }
    }
}


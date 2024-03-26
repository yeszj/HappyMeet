package cn.yanhu.baselib.view

import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.graphics.Typeface
import android.text.TextUtils
import android.view.LayoutInflater
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.TextFontStyleUtils

/**
 * 标题文件 属性：titleLeftArrow:boolean 返回箭头是否显示，默认显示 titleLeftText:String 左边按钮内容
 * titleName:String 标题名字 titleRight：String 右侧按钮名字
 *
 *
 * 如果需要隐藏左边窗口，需要把titleLeftArrow设置为false，并把titleLeftText设置为"".
 *
 *
 * titleOnClickListener:按钮点击事件。
 *
 *
 * 1.设置左边back 箭头，则显示返回
 *
 *
 * 2.不设置左边箭头，则显示取消
 *
 *
 * 3.可自定义标题
 *
 *
 * 4.可自定义左边按钮文字
 *
 *
 * 5.可自定义右边按钮文字（确定、发送、取消等）
 *
 * @author Administrator
 */
class TitleBar : LinearLayout {
    private var ivBack: ImageView? = null
    private var titleName: TextView? = null
    private var titleRightBtn: TextView? = null
    private var titleView: ViewGroup? = null
    private var flCustomRightView: FrameLayout? = null
    private var tvLeftTitle: TextView? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initView(context)
        if (attrs == null) {
            return
        }
        val attrArray = context.resources.obtainAttributes(attrs, R.styleable.TitleBar)
        val color = attrArray.getColor(R.styleable.TitleBar_titleBgColor, 0)
        val titleColor = attrArray.getColor(R.styleable.TitleBar_titleColor, 0)
        val titleRightColor = attrArray.getColor(R.styleable.TitleBar_titleRightColor, 0)
        val aBoolean = attrArray.getBoolean(R.styleable.TitleBar_titleNameIsbold, true)
        if (aBoolean) {
            titleName!!.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        } else {
            titleName!!.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
        val rightIsbold = attrArray.getBoolean(R.styleable.TitleBar_titleRightNameIsbold, true)
        if (rightIsbold) {
            TextFontStyleUtils.setTextFontStyle(
                titleRightBtn!!,
                context.getString(R.string.fontMedium)
            )
        } else {
            TextFontStyleUtils.setTextFontStyle(
                titleRightBtn!!,
                context.getString(R.string.fontRegular)
            )
        }
        if (titleRightColor != 0) {
            titleRightBtn!!.setTextColor(titleRightColor)
        }
        if (titleColor != 0) {
            titleName!!.setTextColor(titleColor)
            tvLeftTitle!!.setTextColor(titleColor)
        }
        if (color != 0) {
            titleView!!.setBackgroundColor(color)
        }
        // 设置左边返回按钮上的文字以及箭头是否显示。默认箭头显示，文字为返回
        val leftDrawable = attrArray.getDrawable(R.styleable.TitleBar_titleLeftIco)
        if (leftDrawable != null) {
            ivBack!!.setImageDrawable(leftDrawable)
        } else {
            ivBack!!.setImageResource(R.drawable.svg_black_back)
        }
        if (attrArray.getBoolean(R.styleable.TitleBar_titleShowBackButton, true)) {
            ivBack!!.visibility = VISIBLE
        } else {
            ivBack!!.visibility = GONE
        }

        // 设置titleName
        val title = attrArray.getString(R.styleable.TitleBar_titleName)
        if (!TextUtils.isEmpty(title)) {
            titleName!!.text = title
        }

        // 设置左边文字的内容
        val titleLeftName = attrArray.getString(R.styleable.TitleBar_titleLeftName)
        if (!TextUtils.isEmpty(titleLeftName)) {
            tvLeftTitle!!.text = titleLeftName
        }
        // 设置右边文字的内容
        val rightText = attrArray.getString(R.styleable.TitleBar_titleRightName)
        val rightDrawable = attrArray.getDrawable(R.styleable.TitleBar_titleRightIco)
        val titleRightDrawableLeft = attrArray.getBoolean(R.styleable.TitleBar_titleRightDrawableLeft,true)
        val titleRightLayoutId =
            attrArray.getResourceId(R.styleable.TitleBar_titleRightLayoutId, -1)

        if (titleRightLayoutId != -1) {
            val inflate = LayoutInflater.from(context).inflate(titleRightLayoutId, null)
            flCustomRightView?.addView(inflate)
            inflate.setOnClickListener { view1: View? ->
                titleBtnClickListener?.rightButtonOnClick(
                    view1
                )
            }
        }

        if (rightDrawable != null) {
            titleRightBtn!!.setCompoundDrawablesWithIntrinsicBounds(
                if (titleRightDrawableLeft) rightDrawable else null,
                null,
                if (titleRightDrawableLeft) null else rightDrawable,
                null
            )
        }
        if (!TextUtils.isEmpty(rightText)) {
            titleRightBtn!!.visibility = VISIBLE
            titleRightBtn!!.text = rightText
            titleRightBtn!!.setOnClickListener(listener)
        } else {
            titleRightBtn!!.text = ""
            if (rightDrawable != null) {
                titleRightBtn!!.setOnClickListener(listener)
            }
        }
        ivBack!!.setOnClickListener(listener)
        attrArray.recycle()
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.title_bar, this, true)
        ivBack = findViewById(R.id.iv_back)
        titleName = findViewById(R.id.titleName)
        titleRightBtn = findViewById(R.id.title_right_btn)
        titleView = findViewById(R.id.titleView)
        flCustomRightView = findViewById(R.id.fl_customRightView)
        tvLeftTitle = findViewById(R.id.tv_leftTitle)
    }


    fun setCustomRightView(layoutId: Int) {
        val view = LayoutInflater.from(context).inflate(layoutId, null)
        flCustomRightView!!.addView(view)
        view.setOnClickListener { view1: View? -> titleBtnClickListener?.rightButtonOnClick(view1) }
    }

    fun getRightView(): View? {
        return flCustomRightView
    }

    fun getTitleView(): View {
        return this
    }

    fun getTitleRightView(): View? {
        return titleRightBtn
    }

    private var titleBtnClickListener: TitleButtonOnClickListener? =
        object : TitleButtonOnClickListener {
            override fun rightButtonOnClick(v: View?) {}
            override fun leftButtonOnClick(v: View?) {
                if (context is Activity) {
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(
                        R.anim.slide_left_in,
                        R.anim.slide_right_out
                    )
                }
            }
        }
    private var listener = OnClickListener { v ->
        val i = v.id
        if (i == R.id.iv_back) {
            titleBtnClickListener?.leftButtonOnClick(v)
        } else if (i == R.id.title_right_btn) {
            titleRightBtn?.isEnabled = false
            titleBtnClickListener?.rightButtonOnClick(v)
            titleRightBtn?.isEnabled = true
        }
    }

    /**
     * 标题中的按钮被点击监听
     *
     * @author Administrator
     */
    interface TitleButtonOnClickListener {
        /**
         * 左边按钮被点击
         *
         * @param v
         */
        fun leftButtonOnClick(v: View?)

        /**
         * 右边按钮被点击
         *
         * @param v
         */
        fun rightButtonOnClick(v: View?)
    }

    /**
     * 设置标题按钮被点击的事件
     *
     * @param titleBtnClickListener
     */
    fun setTitleButtonOnClickListener(titleBtnClickListener: TitleButtonOnClickListener?) {
        this.titleBtnClickListener = titleBtnClickListener
    }

    /**
     * 设置titleName
     *
     * @param title
     */
    fun setTitleName(title: String?) {
        titleName!!.text = title
    }

    fun setLeftTitleName(title: String?) {
        tvLeftTitle!!.text = title
    }

    fun setTitleNameColor(colorId: Int) {
        titleName!!.setTextColor(ContextCompat.getColor(context, colorId))
    }

    fun setTitleAlphaColor(colorId: Int) {
        titleName!!.setTextColor(colorId)
        titleRightBtn!!.setTextColor(colorId)
    }

    /**
     * 设置右边按钮内容
     *
     * @param title
     */
    fun setTitleRightText(title: String?) {
        if (TextUtils.isEmpty(title)) {
            titleRightBtn!!.visibility = GONE
        } else {
            titleRightBtn!!.visibility = VISIBLE
            titleRightBtn!!.text = title
        }

    }

    fun setRightDrawable(drawableId: Int) {
        val rightDrawable = ContextCompat.getDrawable(context, drawableId)
        titleRightBtn!!.setCompoundDrawablesWithIntrinsicBounds(rightDrawable, null, null, null)
    }

    fun setTitleRightDrawable(drawable: Drawable? = null) {
        titleRightBtn!!.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }


    fun setRightVisible(visible: Int) {
        titleRightBtn!!.visibility = visible
    }

    fun setLeftDrawable(drawableId: Int) {
        val leftDrawable = ContextCompat.getDrawable(context, drawableId)
        ivBack?.setImageDrawable(leftDrawable)
    }

    fun setShowBack(visible: Int) {
        ivBack?.visibility = visible
    }

    fun setTitleRightTextSize(size_sp: Int) {
        titleRightBtn!!.textSize = size_sp.toFloat()
    }

    fun setBackGroundColor(colorId: Int) {
        titleView!!.setBackgroundColor(ContextCompat.getColor(context, colorId))
    }
}
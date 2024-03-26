package cn.yanhu.baselib.view;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.core.content.ContextCompat;

import cn.yanhu.baselib.R;
import cn.yanhu.baselib.utils.DisplayUtils;


public class ClearEditText extends CustomFontEditText implements OnFocusChangeListener, TextWatcher {

    /**
     * 删除按钮的引用
     */
    private Drawable mClearDrawable;
    private boolean hasFoucs;

    public ClearEditText(Context context) {

        super(context);
    }

    public ClearEditText(Context context, AttributeSet attrs) {

        // 通过这个构造方法可以在XML中定义属性，如果没有该构造函数，xml中定义的属性无法执行
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context) {

        // 获取EditText的DrawableRight，如果没有，则使用默认图片。
        mClearDrawable = getCompoundDrawables()[2];
        Drawable leftDrawable = getCompoundDrawables()[0];
        if (leftDrawable != null) {
            leftDrawable.setBounds(0, 0, DisplayUtils.dip2px(16), DisplayUtils.dip2px(16));
        }

        if (mClearDrawable == null) {
            mClearDrawable = ContextCompat.getDrawable(context,R.drawable.icon_delete);
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        } else {
            mClearDrawable.setBounds(0, 0, DisplayUtils.dip2px(16), DisplayUtils.dip2px(16));
        }

        setClearIcoVisible(false);
        // 为输入框设置焦点改变监听，如果输入框有焦点，我们判断输入框的值是否为空，为空就隐藏清除图标，否则就显示
        setOnFocusChangeListener(this);

        addTextChangedListener(this);

    }

    /**
     * 设置图标的显示和隐藏，调用setCompoundDrawables为EditText绘制上去。
     */
    private void setClearIcoVisible(boolean visible) {
        Drawable rightDrawable = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], rightDrawable, getCompoundDrawables()[3]);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 设置文本框焦点发生变化时，删除图标的显示与隐藏。 1.如果失去焦点，删除图标不可见， 2.如果有焦点，并且文本长度为0，删除图标也不可见。
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        this.hasFoucs = hasFocus;
        if (hasFocus) {
            setClearIcoVisible(getText().length() > 0);
        } else {
            setClearIcoVisible(false);
        }

    }

    /**
     * 当手指抬起的位置在clean的图标的区域 我们将此视为进行清除操作 getWidth():得到控件的宽度
     * event.getX():抬起时的坐标(改坐标是相对于控件本身而言的)
     * getTotalPaddingRight():clean的图标左边缘至控件右边缘的距离
     * getPaddingRight():clean的图标右边缘至控件右边缘的距离 于是: getWidth() -
     * getTotalPaddingRight()表示: 控件左边到clean的图标左边缘的区域 getWidth() -
     * getPaddingRight()表示: 控件左边到clean的图标右边缘的区域 所以这两者之间的区域刚好是clean的图标的区域
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Drawable drawable = getCompoundDrawables()[2];
            if (drawable != null && event.getX() <= (getWidth() - getPaddingRight())
                    && event.getX() >= (getWidth() - getPaddingRight() - drawable.getBounds().width())) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 文本框中内容发生变化时判断内容长度是否为空
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        if (hasFoucs) {
            setClearIcoVisible(length() > 0);
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }


    /**
     * 设置晃动动画
     */
    public void setShakeAnimation() {
        this.requestFocus();
        this.setFocusableInTouchMode(true);
        this.setFocusable(true);
        this.startAnimation(shakeAnimation(5));
    }

    /**
     * 晃动动画
     *
     * @param counts 1秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts) {

        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }
}

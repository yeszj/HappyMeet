package cn.yanhu.baselib.widget.indicator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;


import net.lucode.hackware.magicindicator.buildins.ArgbEvaluatorHolder;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IMeasurablePagerTitleView;

import cn.yanhu.baselib.R;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.TextFontStyleUtils;


/**
 * @author: witness
 * created: 2022/11/24
 * desc:
 */
@SuppressLint("ViewConstructor")
public class CommonIndicatorTitleView extends AppCompatTextView implements IMeasurablePagerTitleView {
    protected int mSelectedColor;
    protected int mNormalColor;

    public CommonIndicatorTitleView(Context context, int paddingLeft) {
        super(context, null);
        init(context,paddingLeft);
    }

    private void init(Context context,int paddingLeft) {
        setGravity(Gravity.CENTER);
        setPadding(paddingLeft,0,paddingLeft,0);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public int getContentLeft() {
        Rect bound = new Rect();
        String longestString = "";
        if (getText().toString().contains("\n")) {
            String[] brokenStrings = getText().toString().split("\\n");
            for (String each : brokenStrings) {
                if (each.length() > longestString.length()) longestString = each;
            }
        } else {
            longestString = getText().toString();
        }
        getPaint().getTextBounds(longestString, 0, longestString.length(), bound);
        int contentWidth = bound.width();
        return getLeft() + getWidth() / 2 - contentWidth / 2;
    }

    @Override
    public int getContentTop() {
        Paint.FontMetrics metrics = getPaint().getFontMetrics();
        float contentHeight = metrics.bottom - metrics.top;
        return (int) (getHeight() / 2 - contentHeight / 2);
    }

    @Override
    public int getContentRight() {
        Rect bound = new Rect();
        String longestString = "";
        if (getText().toString().contains("\n")) {
            String[] brokenStrings = getText().toString().split("\\n");
            for (String each : brokenStrings) {
                if (each.length() > longestString.length()) longestString = each;
            }
        } else {
            longestString = getText().toString();
        }
        getPaint().getTextBounds(longestString, 0, longestString.length(), bound);
        int contentWidth = bound.width();
        return getLeft() + getWidth() / 2 + contentWidth / 2;
    }

    @Override
    public int getContentBottom() {
        Paint.FontMetrics metrics = getPaint().getFontMetrics();
        float contentHeight = metrics.bottom - metrics.top;
        return (int) (getHeight() / 2 + contentHeight / 2);
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int normalColor) {
        mNormalColor = normalColor;
    }

    private Float mSelectTextSize;
    private Float mNormalTextSize;
    public void setSelectTextSize(Float selectTextSize){
        mSelectTextSize = selectTextSize;
    }

    public void setNormalTextSize(Float normalTextSize){
        mNormalTextSize = normalTextSize;
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        int color = ArgbEvaluatorHolder.eval(leavePercent, mSelectedColor, mNormalColor);
        setTextColor(color);
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        int color = ArgbEvaluatorHolder.eval(enterPercent, mNormalColor, mSelectedColor);
        setTextColor(color);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        setTextColor(mSelectedColor);
        setTextSize(mSelectTextSize);
        TextFontStyleUtils.setTextFontStyle(this, CommonUtils.getString(R.string.fontBold));
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        setTextColor(mNormalColor);
        setTextSize(mNormalTextSize);
        TextFontStyleUtils.setTextFontStyle(this, CommonUtils.getString(R.string.fontRegular));
    }
}

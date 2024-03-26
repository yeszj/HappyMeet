package cn.yanhu.baselib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import cn.yanhu.baselib.R;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.TextFontStyleUtils;
import cn.yanhu.baselib.utils.TextViewDrawableUtils;


/**
 * @author zhengjun
 * @desc 继承Android自带TabLayout自定义tablelayout
 * @create at 2019/8/26 16:56
 */
public class CustomTabLayout extends TabLayout {
    private Context mContext;
    private ViewPager2 viewpager;
    private int normalTextColor;
    private int selectTextColor;
    private float normalTextSize;
    private float selectTextSize;
    private boolean isBold;
    private int customSelectBg;
    private int customNormalBg;
    private int showIconPosition;

    public CustomTabLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        this.mContext = context;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout);
            //默认颜色
            normalTextColor = array.getColor(R.styleable.CustomTabLayout_customNormalTextColor, Color.parseColor("#969697"));//颜色
            //选中时颜色
            selectTextColor = array.getColor(R.styleable.CustomTabLayout_customSelectTextColor, Color.parseColor("#333333"));//颜色
            //默认字体大小
            normalTextSize = array.getDimensionPixelSize(R.styleable.CustomTabLayout_customNormalTextSize, 14);
            //选中时字体大小
            selectTextSize = array.getDimensionPixelSize(R.styleable.CustomTabLayout_customSelectTextSize, 14);
            //选中时字体是否加粗
            isBold = array.getBoolean(R.styleable.CustomTabLayout_customSelectIsBold, true);
            customSelectBg = array.getResourceId(R.styleable.CustomTabLayout_customSelectBg,0);
            customNormalBg =  array.getResourceId(R.styleable.CustomTabLayout_customNormalBg,0);
            showIconPosition = array.getInt(R.styleable.CustomTabLayout_showIconPosition,0);
            this.setSelectedTabIndicatorColor(mContext.getResources().getColor(R.color.transparent));
            this.setTabMode(MODE_AUTO);
            array.recycle();
        }
    }

    public void initViewPager(ViewPager2 viewPager, List<String> titles, int drawableId) {
        this.titles = titles;
        this.drawableId = drawableId;
        addOnTabSelectedListener();
        this.viewpager = viewPager;
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(this, viewPager, this::setTabValue);
        tabLayoutMediator.attach();
    }


    private void setTabValue(Tab tab, int position) {
        if (tab != null) {
            tab.setCustomView(R.layout.indicator_tab_bg_view);
            View customView = tab.getCustomView();
            if (customView != null) {
                LinearLayout layout = tab.view;
                layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));
                TextView tv_tab_title = customView.findViewById(R.id.tv_tab_title);
                tv_tab_title.setText(titles.get(position));
                if (position == showIconPosition) {
                    if (drawableId != -1) {
                        TextViewDrawableUtils.setDrawableLeft(tv_tab_title, ContextCompat.getDrawable(mContext, drawableId));
                    }
                }
                if (position == 0) {
                    setBgSelect(tab);
                } else {
                    setBgUnSelect(tab);
                }
            }
        }
    }

    private List<String> titles;
    private int drawableId;

    /**
     * @desc 初始化tab栏
     * @author zhengjun
     */
    private void addOnTabSelectedListener() {
        addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                if (tab.getCustomView() != null) {
                    setBgSelect(tab);
                }
                viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {
                if (tab.getCustomView() != null) {
                    setBgUnSelect(tab);
                }
            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });
    }

    //背景选中样式
    private void setBgSelect(Tab tab) {
        View customView = tab.getCustomView();
        if (customView != null) {
            TextView tv_tab_title = customView.findViewById(R.id.tv_tab_title);
            tv_tab_title.setBackgroundResource(customSelectBg);
            tv_tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectTextSize);
            tv_tab_title.setTextColor(selectTextColor);
            if (isBold) {
                TextFontStyleUtils.setTextFontStyle(tv_tab_title, CommonUtils.getString(R.string.fontBold) );
            } else {
                TextFontStyleUtils.setTextFontStyle(tv_tab_title, CommonUtils.getString(R.string.fontRegular) );
            }
        }
    }

    //背景默认样式
    private void setBgUnSelect(Tab tab) {
        View customView = tab.getCustomView();
        if (customView != null) {
            TextView tv_tab_title = customView.findViewById(R.id.tv_tab_title);
            tv_tab_title.setBackgroundResource(customNormalBg);
            tv_tab_title.setTextColor(normalTextColor);
            tv_tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, normalTextSize);
            TextFontStyleUtils.setTextFontStyle(tv_tab_title, CommonUtils.getString(R.string.fontRegular) );
        }
    }
}

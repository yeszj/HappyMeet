/*
 * AUTHOR: Zhenjie Yan
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © www.mamaqunaer.com. All Rights Reserved
 *
 */
package cn.yanhu.baselib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.Display;

import java.lang.reflect.Field;

/**
 * Created by Yan Zhenjie on 2016/7/7.
 */
public class DisplayUtils {

    private static boolean isInitialize = false;
    /**
     * 屏幕宽度
     **/
    public static int screenWidth;
    /**
     * 屏幕高度
     **/
    public static int screenHeight;
    /**
     * 状态栏高度
     */
    public static int statusBarHeight;
    /**
     * 屏幕密度
     **/
    public static int screenDpi;
    /**
     * 逻辑密度, 屏幕缩放因子
     */
    public static float density = 1;
    /**
     * 字体缩放因子
     */
    public static float scaledDensity;

    /**
     * actionbar高度
     */
    public static int actionbarHeight;

    /**
     * 初始化屏幕宽度和高度
     */
    public static void initScreen(Activity activity) {
        if (isInitialize) return;
        isInitialize = true;
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metric = new DisplayMetrics();

        // 屏幕宽度、高度、密度、缩放因子
        if (VERSION.SDK_INT >= 17) {
            display.getRealMetrics(metric);
        } else {
            display.getMetrics(metric);
        }

        try {
            // 状态栏高度
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Field field = clazz.getField("status_bar_height");
            int x = Integer.parseInt(field.get(clazz.newInstance()).toString());
            statusBarHeight = activity.getResources().getDimensionPixelSize(x);
        } catch (Throwable e) {
            e.printStackTrace();
            Rect outRect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
            statusBarHeight = outRect.top;
        }

        try {
            TypedArray actionbarSizeTypedArray = activity.obtainStyledAttributes(
                new int[] {android.R.attr.actionBarSize});
            float h = actionbarSizeTypedArray.getDimension(0, 0);
            actionbarHeight = (int)h;
        } catch (Exception e) {
            e.printStackTrace();
        }

        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;
        screenDpi = metric.densityDpi;
        density = metric.density;
        scaledDensity = metric.scaledDensity;
    }

    public static int px2dip(float inParam) {
        return (int) (inParam / density + 0.5F);
    }

    public static int dip2px(float inParam) {
        return (int) (inParam * density + 0.5F);
    }

    public static int px2sp(float inParam) {
        return (int) (inParam / scaledDensity + 0.5F);
    }

    public static int sp2px(float inParam) {
        return (int) (inParam * scaledDensity + 0.5F);
    }

    public static int dp2px(Context ctx, float dip) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return (int) (dip * density);
    }
}
package com.pcl.sdklib.sdk.jverrify;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

/**
 * @author: zhengjun
 * created: 2024/8/7
 * desc:
 */
public class JiguanToast extends Toast {
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public JiguanToast(Context context, OnShowListener onShowListener) {
        super(context);
        this.onShowListener = onShowListener;
    }

    private OnShowListener onShowListener;
    @Override
    public void show() {
        //super.show();
        onShowListener.onShow();
    }
    public interface OnShowListener{
        void onShow();
    }
}

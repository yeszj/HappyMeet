package cn.yanhu.baselib.refresh;

import android.content.Context;
import android.util.AttributeSet;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;


/**
 * @author zhengjun
 * @desc MyRefreshLayout 继承第三方SmartRefreshLayout刷新控件
 * 后期如果切换刷新控件，只需把继承类变一下就行了，布局文件就不需要切换
 * @create at 2019/10/22 10:54
 */
public class MyRefreshLayout extends SmartRefreshLayout {
    public MyRefreshLayout(Context context) {
        super(context);
    }

    public MyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}

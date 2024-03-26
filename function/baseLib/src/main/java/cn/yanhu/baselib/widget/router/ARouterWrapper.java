package cn.yanhu.baselib.widget.router;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

import cn.zj.netrequest.BuildConfig;


public class ARouterWrapper {

    public static void init(Application application) {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(application);
    }

    public static void destory(){
        ARouter.getInstance().destroy();
    }

}

package cn.yanhu.baselib.refresh;

import android.content.Context;
import android.view.ViewGroup;


/**
 * @author zhengjun
 * @desc 自动刷新管理类封装，方便后期切换刷新框架
 * 只需重新实现一个IrefreshProcessor 即可
 * @create at 2019/8/16 11:27
 */
public class RefreshManager implements IrefreshProcessor {
    private static IrefreshProcessor irefreshProcessor = null;
    private static volatile RefreshManager instance;

    private RefreshManager() {
    }

    public static RefreshManager getInstance() {
        if (instance == null) {
            synchronized (RefreshManager.class) {
                if (instance == null) {
                    instance = new RefreshManager();
                }
            }
        }
        return instance;
    }

    public static void init(IrefreshProcessor refreshProcessor) {
        irefreshProcessor = refreshProcessor;
    }

    @Override
    public void initRefresh(Context context, boolean isLoadMore, ViewGroup view, IRefreshCallBack callback) {
        irefreshProcessor.initRefresh(context,isLoadMore, view, callback);
    }
}

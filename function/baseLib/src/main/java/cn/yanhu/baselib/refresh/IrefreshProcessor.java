package cn.yanhu.baselib.refresh;

import android.content.Context;
import android.view.ViewGroup;


/**
 * @author zhengjun
 * @desc
 * @create at 2019/8/16 11:32
 */
public interface IrefreshProcessor {
    void initRefresh(Context context,boolean isLoadMore, ViewGroup view, IRefreshCallBack callback);
}

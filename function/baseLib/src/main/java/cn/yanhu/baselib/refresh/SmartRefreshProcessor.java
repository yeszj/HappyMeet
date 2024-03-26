package cn.yanhu.baselib.refresh;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;

import cn.yanhu.baselib.R;


/**
 * @author zhengjun
 * @desc SmartRefreshLayout代理类
 * @create at 2019/10/22 10:30
 */
public class SmartRefreshProcessor implements IrefreshProcessor {
    @Override
    public void initRefresh(Context context,  final boolean isLoadMore, final ViewGroup refreshLayout, final IRefreshCallBack callback) {
        if (refreshLayout instanceof SmartRefreshLayout) {
            ((SmartRefreshLayout) refreshLayout).setOnRefreshListener(refreshLayout1 -> {
                ((SmartRefreshLayout) refreshLayout).setEnableLoadMore(isLoadMore);
                callback.onRefresh();
            });
            ((SmartRefreshLayout) refreshLayout).setOnLoadMoreListener(refreshLayout12 -> {
                    callback.onLoadMore();
            });
            ((SmartRefreshLayout) refreshLayout).setRefreshFooter(new ClassicsFooter(context)
                    .setSpinnerStyle(SpinnerStyle.Scale));
            //设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator((new DefaultRefreshHeaderCreator() {
                @NonNull
                @Override
                public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
                    layout.setPrimaryColorsId(R.color.refreshColor, R.color.colorTextGrey);//全局设置主题颜色
                    return new MaterialHeader(context);
                }
            }));
            ((SmartRefreshLayout) refreshLayout).setEnableLoadMore(isLoadMore);
        }
    }
}

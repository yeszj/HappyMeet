package cn.yanhu.baselib.pop;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.core.AttachPopupView;
import com.lxj.xpopup.util.XPopupUtils;
import com.lxj.xpopup.widget.VerticalRecyclerView;
import com.lxj.xpopup.R;

import java.util.List;

import cn.yanhu.baselib.adapter.AttachListAdapter;
import cn.yanhu.baselib.bean.AttachParamsInfo;

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
public class CommonAttachListPopupView extends AttachPopupView {
    RecyclerView recyclerView;
    protected int contentGravity = Gravity.CENTER;

    /**
     * @param context
     */
    public CommonAttachListPopupView(@NonNull Context context) {
        super(context);
        addInnerContent();
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout._xpopup_attach_impl_list;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        AttachListAdapter attachListAdapter = new AttachListAdapter();
        attachListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (selectListener != null) {
                selectListener.onSelect(position, attachListAdapter.getItem(position));
            }
            if (popupInfo.autoDismiss) dismiss();
        });
        recyclerView.setAdapter(attachListAdapter);
        attachListAdapter.submitList(paramsInfoList);
        applyTheme();
    }

    protected void applyTheme() {
        if (popupInfo.isDarkTheme) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
        attachPopupContainer.setBackground(XPopupUtils.createDrawable(getResources().getColor(popupInfo.isDarkTheme ? R.color._xpopup_dark_color
                : R.color._xpopup_light_color), popupInfo.borderRadius));
    }

    @Override
    protected void applyDarkTheme() {
        super.applyDarkTheme();
        ((VerticalRecyclerView) recyclerView).setupDivider(true);
    }

    @Override
    protected void applyLightTheme() {
        super.applyLightTheme();
        ((VerticalRecyclerView) recyclerView).setupDivider(false);
    }

    List<AttachParamsInfo> paramsInfoList;

    public CommonAttachListPopupView setStringData(List<AttachParamsInfo> paramsInfoList) {
        this.paramsInfoList = paramsInfoList;
        return this;
    }

    public CommonAttachListPopupView setContentGravity(int gravity) {
        this.contentGravity = gravity;
        return this;
    }

    private OnAttachSelectListener selectListener;

    public CommonAttachListPopupView setOnSelectListener(OnAttachSelectListener selectListener) {
        this.selectListener = selectListener;
        return this;
    }

    public interface OnAttachSelectListener{
        void onSelect(int position,AttachParamsInfo attachParamsInfo);
    }
}
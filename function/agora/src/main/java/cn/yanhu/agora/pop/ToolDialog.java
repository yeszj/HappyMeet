package cn.yanhu.agora.pop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.yanhu.agora.R;
import cn.yanhu.agora.adapter.ToolsAdapter;
import cn.yanhu.agora.bean.ToolBean;
import cn.yanhu.agora.databinding.DialogToolBinding;
import cn.yanhu.baselib.base.BaseSheetDialog;
import cn.yanhu.commonres.bean.RoomDetailInfo;
import cn.yanhu.commonres.config.EventBusKeyConfig;
import cn.yanhu.commonres.manager.LiveDataEventManager;
import cn.yanhu.commonres.router.RouteIntent;

public class ToolDialog extends BaseSheetDialog<DialogToolBinding> {

    private DialogToolBinding binding;

    private RoomDetailInfo liveRoomInfo;

    public interface OnClickListener {
        void onClickWarning();

        void onClickClose();

        void onStickyRoom();
    }

    private OnClickListener onClickListener;

    public void setLiveRoomInfo(RoomDetailInfo liveRoomInfo, OnClickListener onClickListener) {
        this.liveRoomInfo = liveRoomInfo;
        this.onClickListener = onClickListener;
    }

    @Nullable
    @Override
    protected DialogToolBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return DialogToolBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getBinding() != null) {
            binding = getBinding();
        }

        initData();

        binding.dgCancel.setOnClickListener(v -> dismiss());
    }

    private void initData() {
        List<ToolBean> toolBeanList = new ArrayList<>();
        toolBeanList.add(new ToolBean(cn.yanhu.commonres.R.drawable.svg_network, "网络状态"));
        toolBeanList.add(new ToolBean(cn.yanhu.commonres.R.drawable.svg_room_report, "投诉举报"));
        if (liveRoomInfo.isAdmin()) {
            toolBeanList.add(new ToolBean(cn.yanhu.commonres.R.drawable.svg_room_top, "置顶房间"));
            toolBeanList.add(new ToolBean(R.drawable.svg_report, "发送警告"));
            toolBeanList.add(new ToolBean(cn.yanhu.commonres.R.drawable.svg_room_close, "强制关房"));
        }
        ToolsAdapter toolAdapter = new ToolsAdapter();
        binding.dgToolRv.setAdapter(toolAdapter);
        toolAdapter.submitList(toolBeanList);
        toolAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            ToolBean toolBean = toolAdapter.getItem(i);
            if (toolBean==null){
                return;
            }
            String name = toolBean.getName();
            if ("网络状态" .equals(name)) {
                LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.CHECK_NET,true);
            } else if ("发送警告" .equals(name)) {
                onClickListener.onClickWarning();
            } else if ("强制关房" .equals(name)) {
                onClickListener.onClickClose();
            }else if ("置顶房间" .equals(name)) {
                onClickListener.onStickyRoom();
            }else if ("投诉举报" .equals(name)) {
                RouteIntent.INSTANCE.lunchReportPage("");
            }
        });
    }

}

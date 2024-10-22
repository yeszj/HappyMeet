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

public class ToolDialog extends BaseSheetDialog<DialogToolBinding> {

    private DialogToolBinding binding;

    private RoomDetailInfo liveRoomInfo;

    public interface OnClickListener {
        void onClickWarning();

        void onClickClose();
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

        binding.dgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initData() {
        List<ToolBean> toolBeanList = new ArrayList<>();
       // toolBeanList.add(new ToolBean(R.drawable.ic_exception_upload, "异常反馈"));
        toolBeanList.add(new ToolBean(R.drawable.svg_network, "网络状态"));
        if (liveRoomInfo.getAdmin() == 1) {
           // toolBeanList.add(new ToolBean(R.drawable.svg_report, "巡查记录"));
//            toolBeanList.add(new ToolBean(R.drawable.svg_report, "警告"));
            toolBeanList.add(new ToolBean(R.drawable.svg_report, "关房"));
        }
        ToolsAdapter toolAdapter = new ToolsAdapter();
        binding.dgToolRv.setAdapter(toolAdapter);
        toolAdapter.submitList(toolBeanList);
        toolAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            ToolBean toolBean = toolAdapter.getItem(i);
            String name = toolBean.getName();
            if ("网络状态" .equals(name)) {
                LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.CHECK_NET,true);
            } else if ("警告" .equals(name)) {
                onClickListener.onClickWarning();
            } else if ("关房" .equals(name)) {
                onClickListener.onClickClose();
            }
        });
    }


}

package cn.yanhu.imchat.pop;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.jeremyliao.liveeventbus.LiveEventBus;

import cn.yanhu.baselib.base.BaseSheetDialog;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.ViewUtils;
import cn.yanhu.commonres.config.EventBusKeyConfig;
import cn.yanhu.commonres.manager.LiveDataEventManager;
import cn.yanhu.imchat.databinding.DialogImChatBinding;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.ui.chat.ImChatFrg;

public class ImChatDialog extends BaseSheetDialog<DialogImChatBinding> {


    private Bundle extras;

    public ImChatDialog(){

    }

    public ImChatDialog(Bundle bundle){
        extras = bundle;
    }

    @Nullable
    @Override
    protected DialogImChatBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return DialogImChatBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.INSTANCE.setViewHeight(getBinding().dgChatListFg, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_600)+CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20));
        ImChatFrg chatFrg = new ImChatFrg();
        extras.putBoolean("isPop",true);
        chatFrg.setArguments(extras);
        getChildFragmentManager().beginTransaction().replace(R.id.dg_chat_list_fg, chatFrg, "chat_im").commit();
        LiveEventBus.get(EventBusKeyConfig.CLOSECHATDIALOG).observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                dismissAllowingStateLoss();
            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null) {
            View parentView = getView();
            View parent = (View) parentView.getParent();

            BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);

            parentView.measure(0, 0);

            behavior.setPeekHeight(parentView.getMeasuredHeight());

            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();

            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

            parent.setLayoutParams(layoutParams);
            BottomSheetBehavior.from(getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet)).setHideable(false);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.REFRESH_IM_CONVERSATION,true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}

package cn.yanhu.imchat.conversation.custom;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.normal.viewholder.ConversationTeamViewHolder;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.TextViewDrawableUtils;
import cn.yanhu.baselib.utils.ViewUtils;
import cn.yanhu.imchat.R;

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
class CustomConversationTeamViewHolder extends ConversationTeamViewHolder {
    public CustomConversationTeamViewHolder(@NonNull ConversationViewHolderBinding binding) {
        super(binding);
    }

    @Override
    public void onBindData(ConversationBean data, int position) {
        super.onBindData(data, position);
        int width = CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_60);
        ViewUtils.INSTANCE.setMarginLeft(viewBinding.contentLayout,CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_83));
        ViewUtils.INSTANCE.setViewSize(viewBinding.avatarView,width,width);
        ViewUtils.INSTANCE.setViewSize(viewBinding.avatarLayout,width,width);
        ViewUtils.INSTANCE.setViewHeight(viewBinding.contentLayout,width);
        View vg_content = viewBinding.rootView.findViewById(R.id.vg_content);
        ViewUtils.INSTANCE.setMarginTop(vg_content,CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_43));
        viewBinding.avatarView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        viewBinding.avatarView.setCornerRadius(CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_12));
        View tv_onlineCount = viewBinding.rootView.findViewById(R.id.tv_onlineCount);
        tv_onlineCount.setVisibility(View.VISIBLE);
        if (data.infoData.isStickTop()){
            TextViewDrawableUtils.setDrawableRight(itemView.getContext(), viewBinding.nameTv, cn.yanhu.commonres.R.drawable.icon_msg_top);
        }else {
            TextViewDrawableUtils.setDrawableRight(viewBinding.nameTv,null);
        }
    }
}

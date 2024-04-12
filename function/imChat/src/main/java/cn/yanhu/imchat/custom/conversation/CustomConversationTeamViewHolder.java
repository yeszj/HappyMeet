package cn.yanhu.imchat.custom.conversation;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.lihang.ShadowLayout;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.normal.viewholder.ConversationTeamViewHolder;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.TextViewDrawableUtils;
import cn.yanhu.baselib.utils.ViewUtils;
import cn.yanhu.baselib.view.CustomFontTextView;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.bean.GroupBean;

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
class CustomConversationTeamViewHolder extends ConversationTeamViewHolder {
    public CustomConversationTeamViewHolder(@NonNull ConversationViewHolderBinding binding) {
        super(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindData(ConversationBean data, int position) {
        super.onBindData(data, position);
        ShadowLayout slGroupRole = viewBinding.rootView.findViewById(R.id.sl_groupRole);
        CustomFontTextView tv_onlineCount = viewBinding.rootView.findViewById(R.id.tv_onlineCount);
        tv_onlineCount.setVisibility(View.VISIBLE);
        slGroupRole.setVisibility(View.INVISIBLE);
        int width = CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_60);
        ViewUtils.INSTANCE.setMarginLeft(viewBinding.contentLayout,CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_83));
        ViewUtils.INSTANCE.setViewSize(viewBinding.avatarView,width,width);
        ViewUtils.INSTANCE.setViewSize(viewBinding.avatarLayout,width,width);
        ViewUtils.INSTANCE.setViewHeight(viewBinding.contentLayout,width);
        View vg_content = viewBinding.rootView.findViewById(R.id.vg_content);
        ViewUtils.INSTANCE.setMarginTop(vg_content,CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_43));
        viewBinding.avatarView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        viewBinding.avatarView.setCornerRadius(CommonUtils.INSTANCE.getDimension(com.zj.dimens.R.dimen.dp_12));

        if (data.infoData.isStickTop()){
            TextViewDrawableUtils.setDrawableRight(itemView.getContext(), viewBinding.nameTv, cn.yanhu.commonres.R.drawable.icon_msg_top);
        }else {
            TextViewDrawableUtils.setDrawableRight(viewBinding.nameTv,null);
        }

        Team teamInfo = data.infoData.getTeamInfo();
        if (teamInfo==null){
            return;
        }
        String creator = teamInfo.getCreator();
        String extension = teamInfo.getExtension();

        if (TextUtils.isEmpty(extension)){
            return;
        }
        GroupBean groupBean = GsonUtils.fromJson(extension, GroupBean.class);


        CustomFontTextView tvGroupRole = viewBinding.rootView.findViewById(R.id.tv_groupRole);

        tv_onlineCount.setVisibility(View.VISIBLE);
        slGroupRole.setVisibility(View.VISIBLE);
        tv_onlineCount.setText(groupBean.getOnlineCount()+"人在线");
        if (creator.equals(AppCacheManager.INSTANCE.getUserId())){
            tvGroupRole.setText("我的");
            slGroupRole.setGradientColor(Color.parseColor("#F8459B"),Color.parseColor("#00F8459B"));
        }else if (groupBean.isVisitor()){
            tvGroupRole.setText("游客");
            slGroupRole.setGradientColor(Color.parseColor("#6DC9BA"),Color.parseColor("#006DC9BA"));
        }else {
            slGroupRole.setVisibility(View.INVISIBLE);
        }

    }
}

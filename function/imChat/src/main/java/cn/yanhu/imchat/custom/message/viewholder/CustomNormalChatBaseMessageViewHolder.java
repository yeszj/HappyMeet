// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package cn.yanhu.imchat.custom.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.GsonUtils;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.MsgThreadOption;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageReplayNormalViewBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageRevokedNormalViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.CommonUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ReplayUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.RevokeUIOption;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.List;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.view.CustomFontTextView;
import cn.yanhu.commonres.bean.UserDetailInfo;
import cn.yanhu.commonres.view.UserTagView;
import cn.yanhu.imchat.manager.ImTeamManager;

public class CustomNormalChatBaseMessageViewHolder extends ChatBaseMessageViewHolder {
    private static final String TAG = "NormalChatBaseMessageViewHolder";

    protected NormalChatMessageReplayNormalViewBinding replayBinding;
    // 撤销 ui 的控件集合
    protected NormalChatMessageRevokedNormalViewBinding revokedViewBinding;

    public CustomNormalChatBaseMessageViewHolder(
            @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }


    @Override
    protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
        super.onMessageBackgroundConfig(messageBean);
        boolean isReceivedMsg = MessageHelper.isReceivedMessage(messageBean) || isForwardMsg();
        CommonUIOption commonUIOption = uiOptions.commonUIOption;
        boolean isCustomBgValid = true;
        if (isReceivedMsg) {
            if (commonUIOption.otherUserMessageBg != null) {
                baseViewBinding.contentWithTopLayer.setBackground(commonUIOption.otherUserMessageBg);
            } else if (commonUIOption.otherUserMessageBgRes != null) {
                baseViewBinding.contentWithTopLayer.setBackgroundResource(
                        commonUIOption.otherUserMessageBgRes);
            } else if (properties.getReceiveMessageBg() != null) {
                baseViewBinding.contentWithTopLayer.setBackground(properties.getReceiveMessageBg());
            } else if (properties.receiveMessageRes != null) {
                baseViewBinding.contentWithTopLayer.setBackgroundResource(properties.receiveMessageRes);
            } else {
                isCustomBgValid = false;
            }
        } else {
            if (commonUIOption.myMessageBg != null) {
                baseViewBinding.contentWithTopLayer.setBackground(commonUIOption.myMessageBg);
            } else if (commonUIOption.myMessageBgRes != null) {
                baseViewBinding.contentWithTopLayer.setBackgroundResource(commonUIOption.myMessageBgRes);
            } else if (properties.getSelfMessageBg() != null) {
                baseViewBinding.contentWithTopLayer.setBackground(properties.getSelfMessageBg());
            } else if (properties.selfMessageRes != null) {
                baseViewBinding.contentWithTopLayer.setBackgroundResource(properties.selfMessageRes);
            } else {
                isCustomBgValid = false;
            }
        }
        if (isCustomBgValid) {
            return;
        }
        if (isReceivedMsg) {
            baseViewBinding.contentWithTopLayer.setBackgroundResource(R.drawable.chat_message_other_bg);
        } else {
            baseViewBinding.contentWithTopLayer.setBackgroundResource(R.drawable.chat_message_self_bg);
        }
    }

    @Override
    protected void onLayoutConfig(ChatMessageBean messageBean) {
        super.onLayoutConfig(messageBean);
        ConstraintLayout.LayoutParams messageContainerLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
        ConstraintLayout.LayoutParams messageTopLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
        ConstraintLayout.LayoutParams messageBottomLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
        ConstraintLayout.LayoutParams signalLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.llSignal.getLayoutParams();
        ConstraintLayout.LayoutParams statusLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageStatus.getLayoutParams();
        int size = SizeUtils.dp2px(10);
        // 设置标记
        signalLayoutParams.rightMargin = size;
        signalLayoutParams.leftMargin = size;
        statusLayoutParams.rightMargin = size;
        // 设置消息体
        messageContainerLayoutParams.rightMargin = size;
        messageContainerLayoutParams.leftMargin = size;
        // 设置消息体上部（回复内容）
        messageTopLayoutParams.rightMargin = size;
        messageTopLayoutParams.leftMargin = size;
        // 设置消息体下部
        messageBottomLayoutParams.rightMargin = size;
        messageBottomLayoutParams.leftMargin = size;
        // 非回复消息修改布局文件默认大小
        if (!messageBean.hasReply() && !MessageHelper.isThreadReplayInfo(messageBean)) {
            messageContainerLayoutParams.width = 0;
            messageTopLayoutParams.width = 0;
            messageBottomLayoutParams.width = 0;
        }

        CustomFontTextView tvTag = this.baseViewBinding.baseRoot.findViewById(cn.yanhu.imchat.R.id.tv_tag);
        UserTagView userAge = this.baseViewBinding.baseRoot.findViewById(cn.yanhu.imchat.R.id.userAge);


        if (messageBean.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
            String sessionId = messageBean.getMessageData().getMessage().getSessionId();
            if (!MessageHelper.isReceivedMessage(messageBean) && !this.isForwardMsg()) {
                tvTag.setVisibility(View.GONE);
                userAge.setVisibility(View.GONE);
            } else {
                if (MessageHelper.isReceivedMessage(messageBean)) {
                    String account = messageBean.getMessageData().getMessage().getFromAccount();
                    TeamMember member = ImTeamManager.getTeamMember(sessionId, account);
                    TeamMemberType teamType = member.getType();
                    if (teamType == TeamMemberType.Manager) {
                        tvTag.setText("管理员");
                        tvTag.setBackgroundResource(cn.yanhu.commonres.R.drawable.tag_group_admin);
                        tvTag.setTextColor(CommonUtils.INSTANCE.getColor(cn.yanhu.baselib.R.color.adminTagColor));
                        tvTag.setVisibility(View.VISIBLE);
                    } else if (teamType == TeamMemberType.Owner) {
                        tvTag.setText("群主");
                        tvTag.setBackgroundResource(cn.yanhu.commonres.R.drawable.tag_group_owner);
                        tvTag.setTextColor(CommonUtils.INSTANCE.getColor(cn.yanhu.baselib.R.color.colorMain));
                        tvTag.setVisibility(View.VISIBLE);
                    } else {
                        tvTag.setVisibility(View.GONE);
                    }
                }
                UserInfo fromUser = messageBean.getMessageData().getFromUser();
                if (fromUser != null) {
                    String extension = fromUser.getExtension();
                    UserDetailInfo userDetailInfo = GsonUtils.fromJson(extension, UserDetailInfo.class);
                    if (userDetailInfo != null) {
                        userAge.setUserAge(userDetailInfo.getAge(), userDetailInfo.getGender());
                        userAge.setVisibility(View.VISIBLE);
                    }

                }

            }
        }


    }

    @Override
    protected void onMessageRevoked(ChatMessageBean data) {
        RevokeUIOption revokeUIOption = uiOptions.revokeUIOption;
        if (revokeUIOption.enable != null && !revokeUIOption.enable) {
            return;
        }
        if (!data.isRevoked()) {
            baseViewBinding.messageContainer.setEnabled(true);
            return;
        }
        if (data.hasReply() || MessageHelper.isThreadReplayInfo(data)) {
            ConstraintLayout.LayoutParams messageContainerLayoutParams =
                    (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
            ConstraintLayout.LayoutParams messageTopLayoutParams =
                    (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
            ConstraintLayout.LayoutParams messageBottomLayoutParams =
                    (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
            messageContainerLayoutParams.width = 0;
            messageTopLayoutParams.width = 0;
            messageBottomLayoutParams.width = 0;
        }
        baseViewBinding.messageContainer.setEnabled(false);
        baseViewBinding.messageTopGroup.removeAllViews();
        baseViewBinding.messageContainer.removeAllViews();
        baseViewBinding.messageBottomGroup.removeAllViews();
        //reedit
        addRevokeViewToMessageContainer();
        revokedViewBinding.tvAction.setOnClickListener(
                v -> {
                    if (itemClickListener != null && !isMultiSelect) {
                        itemClickListener.onReEditRevokeMessage(v, position, data);
                    }
                });
        if (MessageHelper.revokeMsgIsEdit(data)) {
            revokedViewBinding.tvAction.setVisibility(View.VISIBLE);
        } else {
            revokedViewBinding.tvAction.setVisibility(View.GONE);
        }
        if (revokeUIOption.revokedTipText != null) {
            revokedViewBinding.messageText.setText(revokeUIOption.revokedTipText);
        }
        if (revokeUIOption.actionBtnText != null) {
            revokedViewBinding.tvAction.setText(revokeUIOption.actionBtnText);
        }
        if (revokeUIOption.actionBtnVisible != null) {
            revokedViewBinding.tvAction.setVisibility(
                    revokeUIOption.actionBtnVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void setReplyInfo(ChatMessageBean messageBean) {
        replyMessage = null;
        ReplayUIOption replayUIOption = uiOptions.replayUIOption;
        if (replayUIOption.enable != null && !replayUIOption.enable) {
            return;
        }
        if (messageBean == null || messageBean.getMessageData() == null) {
            return;
        }
        baseViewBinding.messageTopGroup.removeAllViews();
        ALog.w(TAG, TAG, "setReplyInfo, uuid=" + messageBean.getMessageData().getMessage().getUuid());
        if (messageBean.hasReply()) {
            //自定义回复实现
            addReplayViewToTopGroup();
            String replyUuid = messageBean.getReplyUUid();
            if (!TextUtils.isEmpty(replyUuid)) {
                MessageHelper.getReplyMessageInfo(
                        replyUuid,
                        new FetchCallback<List<IMMessageInfo>>() {
                            @Override
                            public void onSuccess(@Nullable List<IMMessageInfo> param) {
                                replyMessage = null;
                                if (param != null && param.size() > 0) {
                                    replyMessage = param.get(0);
                                }
                                String content = "| " + MessageHelper.getReplyContent(replyMessage);
                                MessageHelper.identifyFaceExpression(
                                        replayBinding.tvReply.getContext(),
                                        replayBinding.tvReply,
                                        content,
                                        ImageSpan.ALIGN_BOTTOM);
                                updateReplayInfoLayoutWidth(messageBean);
                            }

                            @Override
                            public void onFailed(int code) {
                                baseViewBinding.messageTopGroup.removeAllViews();
                            }

                            @Override
                            public void onException(@Nullable Throwable exception) {
                                baseViewBinding.messageTopGroup.removeAllViews();
                            }
                        });
            }

            if (itemClickListener != null) {
                replayBinding.tvReply.setOnClickListener(
                        v -> {
                            if (!isMultiSelect) {
                                itemClickListener.onReplyMessageClick(v, position, replyMessage);
                            }
                        });
            }
        } else if (MessageHelper.isThreadReplayInfo(messageBean)) {
            //thread 回复
            setThreadReplyInfo(messageBean);
        } else {
            baseViewBinding.messageTopGroup.removeAllViews();
        }
    }

    @Override
    protected void setStatus(ChatMessageBean data) {
        super.setStatus(data);
        MessageStatusUIOption messageStatusUIOption = uiOptions.messageStatusUIOption;
        if (messageStatusUIOption.readProcessClickListener != null) {
            baseViewBinding.readProcess.setOnClickListener(
                    messageStatusUIOption.readProcessClickListener);
        } else {
            baseViewBinding.readProcess.setOnClickListener(
                    v ->
                            XKitRouter.withKey(RouterConstant.PATH_CHAT_ACK_PAGE)
                                    .withParam(RouterConstant.KEY_MESSAGE, data.getMessageData().getMessage())
                                    .withContext(v.getContext())
                                    .navigate());
        }
    }

    /// 内部设置 thread 回复消息
    private void setThreadReplyInfo(ChatMessageBean messageBean) {
        MsgThreadOption threadOption = messageBean.getMessageData().getMessage().getThreadOption();
        String replyFrom = threadOption.getReplyMsgFromAccount();
        if (TextUtils.isEmpty(replyFrom)) {
            ALog.w(
                    TAG,
                    "no reply message found, uuid=" + messageBean.getMessageData().getMessage().getUuid());
            baseViewBinding.messageTopGroup.removeAllViews();
            return;
        }
        addReplayViewToTopGroup();

        String replyUuid = threadOption.getReplyMsgIdClient();
        MessageHelper.getReplyMessageInfo(
                replyUuid,
                new FetchCallback<List<IMMessageInfo>>() {
                    @Override
                    public void onSuccess(@Nullable List<IMMessageInfo> param) {
                        if (param != null && param.size() > 0) {
                            replyMessage = param.get(0);
                            String content = "| " + MessageHelper.getReplyContent(replyMessage);
                            MessageHelper.identifyFaceExpression(
                                    replayBinding.tvReply.getContext(),
                                    replayBinding.tvReply,
                                    content,
                                    ImageSpan.ALIGN_BOTTOM);
                            updateReplayInfoLayoutWidth(messageBean);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        replayBinding.tvReply.setVisibility(View.GONE);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        replayBinding.tvReply.setVisibility(View.GONE);
                    }
                });

        if (itemClickListener != null) {
            replayBinding.tvReply.setOnClickListener(
                    v -> {
                        if (!isMultiSelect) {
                            itemClickListener.onReplyMessageClick(v, position, replyMessage);
                        }
                    });
        }
    }

    /// 由于 normal 布局中需要 messageContainer 和 messageTopGroup 相同宽度展示
    private void updateReplayInfoLayoutWidth(ChatMessageBean messageBean) {
        ConstraintLayout.LayoutParams messageContainerLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
        ConstraintLayout.LayoutParams messageTopLayoutParams =
                (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
        messageTopLayoutParams.width = 0;
        messageContainerLayoutParams.width = 0;
        baseViewBinding.messageContainer.setLayoutParams(messageContainerLayoutParams);
        baseViewBinding.messageTopGroup.setLayoutParams(messageTopLayoutParams);
        baseViewBinding.messageContainer.post(
                () -> {
                    if (MessageHelper.isReceivedMessage(messageBean)) {
                        return;
                    }
                    int width1 = baseViewBinding.messageTopGroup.getWidth();
                    int width2 = baseViewBinding.messageContainer.getWidth();
                    int maxWidth = Math.max(width1, width2);
                    messageContainerLayoutParams.width = maxWidth;
                    messageTopLayoutParams.width = maxWidth;
                    baseViewBinding.messageContainer.setLayoutParams(messageContainerLayoutParams);
                    baseViewBinding.messageTopGroup.setLayoutParams(messageTopLayoutParams);
                });
    }

    @Override
    protected void onCommonViewVisibleConfig(ChatMessageBean messageBean) {
        super.onCommonViewVisibleConfig(messageBean);
        if (messageBean.isRevoked()) {
            // 消息撤回，头像、名称不展示
            baseViewBinding.myAvatar.setVisibility(View.GONE);
            baseViewBinding.myName.setVisibility(View.GONE);
            baseViewBinding.otherUserAvatar.setVisibility(View.GONE);
            baseViewBinding.otherUsername.setVisibility(View.GONE);
        }
    }

    // 添加 normal 下的回复布局
    private void addReplayViewToTopGroup() {
        replayBinding =
                NormalChatMessageReplayNormalViewBinding.inflate(
                        LayoutInflater.from(parent.getContext()), baseViewBinding.messageTopGroup, true);
    }

    private void addRevokeViewToMessageContainer() {
        revokedViewBinding =
                NormalChatMessageRevokedNormalViewBinding.inflate(
                        LayoutInflater.from(parent.getContext()), baseViewBinding.messageContainer, true);
    }
}

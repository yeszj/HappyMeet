// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package cn.yanhu.imchat.custom.message;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.CALL_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.MULTI_FORWARD_ATTACHMENT;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_FILE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.RICH_TEXT_ATTACHMENT;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.TIP_MESSAGE_VIEW_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

import cn.yanhu.imchat.custom.message.viewholder.CustomChatAudioMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatCallMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatFileMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatForwardMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatImageMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatNotificationMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatRichTextMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatTextMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatVideoMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatLocationMessageViewHolder;
import cn.yanhu.imchat.custom.message.viewholder.CustomChatTipsMessageViewHolder;

/** 标准皮肤，聊天页面消息ViewHolder工厂类。 */
public abstract class CustomChatMessageViewHolderFactory implements IChatFactory {

  @Override
  public CommonBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    CommonBaseMessageViewHolder viewHolder = createViewHolderCustom(parent, viewType);
    if (viewHolder == null) {
      viewHolder = getViewHolderDefault(parent, viewType);
    }
    return viewHolder;
  }

  public abstract @Nullable CommonBaseMessageViewHolder createViewHolderCustom(
      @NonNull ViewGroup parent, int viewType);

  protected CommonBaseMessageViewHolder getViewHolderDefault(
      @NonNull ViewGroup parent, int viewType) {

    ChatBaseMessageViewHolder viewHolder;
    ChatBaseMessageViewHolderBinding viewHolderBinding =
        ChatBaseMessageViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
      viewHolder = new CustomChatAudioMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
      viewHolder = new CustomChatImageMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
      viewHolder = new CustomChatVideoMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NOTICE_MESSAGE_VIEW_TYPE) {
      viewHolder = new CustomChatNotificationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == TIP_MESSAGE_VIEW_TYPE) {
      viewHolder = new CustomChatTipsMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_FILE) {
      viewHolder = new CustomChatFileMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new CustomChatLocationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == CALL_MESSAGE_VIEW_TYPE) {
      viewHolder = new CustomChatCallMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == MULTI_FORWARD_ATTACHMENT) {
      //custom message
      viewHolder = new CustomChatForwardMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == RICH_TEXT_ATTACHMENT) {
      viewHolder = new CustomChatRichTextMessageViewHolder(viewHolderBinding, viewType);
    } else {
      //default as text message
      viewHolder = new CustomChatTextMessageViewHolder(viewHolderBinding, viewType);
    }

    return viewHolder;
  }

  public abstract int getCustomViewType(ChatMessageBean messageBean);

  @Override
  public int getItemViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      int customViewType = getCustomViewType(messageBean);
      if (customViewType > 0) {
        return customViewType;
      } else {
        return messageBean.getViewType();
      }
    }
    return 0;
  }
}

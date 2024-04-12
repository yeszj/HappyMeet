// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package cn.yanhu.imchat.custom.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class CustomChatRichTextMessageViewHolder extends CustomNormalChatBaseMessageViewHolder {

  protected NormalChatMessageRichTextViewHolderBinding viewBinding;

  public CustomChatRichTextMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
        NormalChatMessageRichTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getMessage().getAttachment() instanceof RichTextAttachment) {
      RichTextAttachment attachment =
          (RichTextAttachment) message.getMessageData().getMessage().getAttachment();
      if (attachment != null) {
        viewBinding.messageTitle.setText(attachment.title);
        if (TextUtils.isEmpty(attachment.body)) {
          viewBinding.messageContent.setVisibility(View.GONE);
        } else {
          viewBinding.messageContent.setVisibility(View.VISIBLE);
          MessageHelper.identifyExpression(
              viewBinding.getRoot().getContext(),
              viewBinding.messageContent,
              attachment.body,
              message.getMessageData().getMessage());
        }
      }
    }
  }
}

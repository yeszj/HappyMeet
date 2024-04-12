// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package cn.yanhu.imchat.custom.message.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageForwardViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

public class CustomChatForwardMessageViewHolder extends CustomNormalChatBaseMessageViewHolder {

  protected NormalChatMessageForwardViewHolderBinding viewBinding;

  public CustomChatForwardMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
        NormalChatMessageForwardViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    ViewGroup.LayoutParams params = viewBinding.placeHolder.getLayoutParams();
    params.width = ScreenUtils.getDisplayWidth();
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    boolean isReceivedMsg = MessageHelper.isReceivedMessage(messageBean) || isForwardMsg();
    if (isReceivedMsg) {
      viewBinding.messageForwardLayout.setBackgroundResource(
          R.drawable.chat_forward_message_other_bg);
    } else {
      viewBinding.messageForwardLayout.setBackgroundResource(
          R.drawable.chat_forward_message_self_bg);
    }
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getMessage().getAttachment()
            instanceof MultiForwardAttachment) {
      MultiForwardAttachment attachment =
          (MultiForwardAttachment) message.getMessageData().getMessage().getAttachment();
      String titleText =
          String.format(
              getMessageContainer()
                  .getContext()
                  .getString(R.string.chat_message_multi_record_title),
              attachment.sessionName);
      viewBinding.messageMultiTitle.setText(titleText);
      if (attachment.abstractsList != null) {
        String contentFormat =
            getMessageContainer()
                .getContext()
                .getString(R.string.chat_message_multi_record_content);
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < attachment.abstractsList.size(); i++) {
          String content =
              String.format(
                  contentFormat,
                  ChatUtils.getEllipsizeMiddleNick(attachment.abstractsList.get(i).senderNick),
                  attachment.abstractsList.get(i).content);
          textBuilder.append(content);
          textBuilder.append("\n");
        }
        if (attachment.abstractsList.size() > 1) {
          textBuilder.deleteCharAt(textBuilder.length() - 1);
        }
        viewBinding.messageText.setText(textBuilder.toString());
      }
    }
  }
}

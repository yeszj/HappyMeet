package com.hyphenate.easeui.modules.chat.interfaces;

import android.net.Uri;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.easeui.interfaces.ChatQuoteMessageProvider;
import com.hyphenate.easeui.modules.chat.EaseChatInputMenu;
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout;

import org.json.JSONObject;

public interface IChatLayout {
    /**
     * 获取聊天列表
     * @return
     */
    EaseChatMessageListLayout getChatMessageListLayout();

    /**
     * 获取输入菜单
     * @return
     */
    EaseChatInputMenu getChatInputMenu();

    /**
     * 获取输入框内容
     * @return
     */
    String getInputContent();

    /**
     * 是否打开正在输入监控
     * @param turnOn
     */
    void turnOnTypingMonitor(boolean turnOn);

    /**
     * 发送文本消息
     * @param content
     */
    void sendTextMessage(String content);

    /**
     * 发送文本消息
     * @param content
     * @param isNeedGroupAck 需要需要群回执
     */
    void sendTextMessage(String content, boolean isNeedGroupAck);

    /**
     * 发送@消息
     * @param content
     */
    void sendAtMessage(String content);

    /**
     * 发送大表情消息
     * @param name
     * @param identityCode
     */
    void sendBigExpressionMessage(String name, String identityCode);

    /**
     * 发送语音消息
     * @param filePath
     * @param length
     */
    void sendVoiceMessage(String filePath, int length);

    /**
     * 发送语音消息
     * @param filePath
     * @param length
     */
    void sendVoiceMessage(Uri filePath, int length);

    /**
     * 发送图片消息
     * @param imageUri
     */
    void sendImageMessage(Uri imageUri);

    /**
     * 发送图片消息
     * @param imageUri
     * @param sendOriginalImage
     */
    void sendImageMessage(Uri imageUri, boolean sendOriginalImage);

    /**
     * 发送定位消息
     * @param latitude
     * @param longitude
     * @param locationAddress
     */
    void sendLocationMessage(double latitude, double longitude, String locationAddress, String buildingName);

    /**
     * 发送视频消息
     * @param videoUri
     * @param videoLength
     */
    void sendVideoMessage(Uri videoUri, int videoLength);

    /**
     * 发送文件消息
     * @param fileUri
     */
    void sendFileMessage(Uri fileUri);

    /**
     * 为消息添加扩展字段
     * @param message
     */
    void addMessageAttributes(EMMessage message);

    /**
     * 发送消息
     * @param message
     */
    void sendMessage(EMMessage message);

    /**
     * 重新发送消息
     * @param message
     */
    void resendMessage(EMMessage message);

    /**
     * 删除消息
     * @param message
     */
    void deleteMessage(EMMessage message);

    /**
     * 撤回消息
     * @param message
     */
    void recallMessage(EMMessage message);

    /**
     * 编辑消息
     * @param messageId
     * @param messageBodyModified
     */
    void modifyMessage(String messageId, EMMessageBody messageBodyModified);

    /**
     * 翻译消息
     * @param message
     * @param isTranslate
     */
    void translateMessage(EMMessage message, boolean isTranslate);

    /**
     * 隐藏翻译
     * @param message
     */
    void hideTranslate(EMMessage message);

    /**
     * Set message quote info.
     * Should call it before sendMessage.
     * @param quoteInfo
     */
    void setMessageQuoteInfo(JSONObject quoteInfo);

    /**
     * 用于监听消息的变化
     * @param listener
     */
    void setOnChatLayoutListener(OnChatLayoutListener listener);

    /**
     * 用于监听发送语音的触摸事件
     * @param voiceTouchListener
     */
    void setOnChatRecordTouchListener(OnChatRecordTouchListener voiceTouchListener);

    /**
     * 消息撤回监听
     * @param listener
     */
    void setOnRecallMessageResultListener(OnRecallMessageResultListener listener);

    /**
     * 设置发送消息前设置属性事件
     * @param sendMsgEvent
     */
    void setOnAddMsgAttrsBeforeSendEvent(OnAddMsgAttrsBeforeSendEvent sendMsgEvent);

    /**
     * 设置翻译监听
     * @param translateListener
     */
    void setOnTranslateListener(OnTranslateMessageListener translateListener);

    /**
     * 设置会话结束的监听。
     * 因为群组销毁，被群管理员踢出群等原因。
     * @param listener
     */
    void setOnChatFinishListener(OnChatFinishListener listener);

    /**
     * 设置编辑消息的监听
     * @param listener
     */
    void setOnEditMessageListener(OnModifyMessageListener listener);

    /**
     * Set quote message provider
     * @param provider
     */
    void setChatQuoteMessageProvider(ChatQuoteMessageProvider provider);
}

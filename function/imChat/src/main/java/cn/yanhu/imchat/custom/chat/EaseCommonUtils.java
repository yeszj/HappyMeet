/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.yanhu.imchat.custom.chat;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.GsonUtils;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMConversation.EMConversationType;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.utils.HanziToPinyin;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.commonres.bean.BaseUserInfo;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.imchat.bean.CallFinishRewardBean;
import cn.yanhu.imchat.bean.GroupCenterTipMsgInfo;
import cn.yanhu.imchat.bean.GroupChatUserInfo;
import cn.yanhu.imchat.bean.GroupInviteRecord;
import cn.yanhu.imchat.bean.GroupUserInfo;


public class EaseCommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * check if network avalable
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * check if sdcard exist
     *
     * @return
     */
    public static boolean isSdcardExist() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static EMMessage createExpressionMessage(String toChatUsername, String expressioName, String identityCode) {
        EMMessage message = EMMessage.createTxtSendMessage("[" + expressioName + "]", toChatUsername);
        if (identityCode != null) {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
        }
        message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        return message;
    }


    public static void showCustomEmojiView(Context context, String customMsg, String sendUserInfo, ViewGroup vgContent, String finalMsgId, boolean isConversationList, boolean isGroup) {
        vgContent.removeAllViews();
        String[] split = customMsg.split("/forlove");
        List<View> list = new ArrayList<>();
        Object msgId = vgContent.getTag(cn.yanhu.commonres.R.id.tag_msg_id);
        Object tag = vgContent.getTag(cn.yanhu.commonres.R.id.tag_emoji_views);
        if (msgId instanceof String && msgId.equals(finalMsgId) && tag instanceof List) {
            for (int i = 0; i < ((List<?>) tag).size(); i++) {
                View view = (View) ((List<?>) tag).get(i);
                vgContent.addView(view);
            }
        } else {
            if (isGroup) {
                try {
                    GroupUserInfo groupUserInfo = GsonUtils.fromJson(
                            sendUserInfo,
                            GroupUserInfo.class
                    );
                    if (groupUserInfo != null) {
                        addTextView(context, vgContent, isConversationList, list, groupUserInfo.getNickName() + "：");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            for (String value : split) {
                if (value.startsWith("{") && value.endsWith("}") && value.contains("bigIconPath") && value.contains("iconPath")) {
                    try {
                        EaseEmojicon easeEmojicon = GsonUtils.fromJson(value, EaseEmojicon.class);
                        LottieAnimationView lottieAnimationView = new LottieAnimationView(context);
                        lottieAnimationView.loop(true);
                        lottieAnimationView.setLayoutParams(new FrameLayout.LayoutParams(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20), CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)));
                        lottieAnimationView.setAnimationFromUrl(easeEmojicon.getBigIconPath());
                        lottieAnimationView.playAnimation();
                        list.add(lottieAnimationView);
                        vgContent.addView(lottieAnimationView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    addTextView(context, vgContent, isConversationList, list, value);
                }
            }
            vgContent.setTag(cn.yanhu.commonres.R.id.tag_msg_id, finalMsgId);
            vgContent.setTag(cn.yanhu.commonres.R.id.tag_emoji_views, list);
        }

    }

    public static void showCustomEmojiView(Context context, EMMessage lastMessage, ViewGroup vgContent, boolean isConversationList, boolean isGroup) {
        String stringAttribute = lastMessage.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
        String sendUserInfo = "";
        if (isGroup) {
            sendUserInfo =
                    lastMessage.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "");
        }
        showCustomEmojiView(context, stringAttribute, sendUserInfo, vgContent, lastMessage.getMsgId(), isConversationList, isGroup);
    }

    private static void addTextView(Context context, ViewGroup vgContent, boolean isConversationList, List<View> list, String value) {
        AppCompatTextView appCompatTextView = new AppCompatTextView(context);
        appCompatTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (isConversationList) {
            appCompatTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            appCompatTextView.setTextColor(Color.parseColor("#888888"));
        } else {
            appCompatTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            appCompatTextView.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common));
        }
        appCompatTextView.setText(replaceLineString(value));
        list.add(appCompatTextView);
        vgContent.addView(appCompatTextView);
    }

    private static String replaceLineString(String value){
        try {
            if (TextUtils.isEmpty(value)){
                return value;
            }else {
                return value.replace("\n"," ");
            }
        }catch (Exception e){
            return value;
        }
    }

    /**
     * Get digest according message type and content
     *
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION:
                if (message.direct() == EMMessage.Direct.RECEIVE) {
                    digest = getString(context, R.string.location_recv);
                    EaseUserProfileProvider userProvider = EaseIM.getInstance().getUserProvider();
                    String from = message.getFrom();
                    if (userProvider != null && userProvider.getUser(from) != null) {
                        EaseUser user = userProvider.getUser(from);
                        if (user != null && !TextUtils.isEmpty(user.getNickname())) {
                            from = user.getNickname();
                        }
                    }
                    digest = String.format(digest, from);
                    return digest;
                } else {
                    digest = getString(context, R.string.location_prefix);
                }
                break;
            case IMAGE:
                digest = getString(context, R.string.picture);
                break;
            case VOICE:
                digest = getString(context, R.string.voice_prefix);
                break;
            case VIDEO:
                digest = getString(context, R.string.video);
                break;
            case CUSTOM:
                EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
                String event = messageBody.event();
                switch (event) {
                    case ChatConstant.MSG_INVITE_SEND_GIFT:
                        digest = "[好友申请]";
                        break;
                    case ChatConstant.MSG_GIFT:
                        digest = "[礼物]";

                        break;
                    case ChatConstant.MSG_REWARD:
                        digest = "[亲密度礼盒]";

                        break;
                    case ChatConstant.MSG_ALERT:
                        digest = "[系统消息]";

                        break;
                    case ChatConstant.MSG_QIANXIAN: {
                        Map<String, String> params = messageBody.getParams();
                        String content = params.get("content");
                        if (!TextUtils.isEmpty(content) && content.startsWith("缘分牵线")) {
                            digest = "[缘分牵线]";
                        } else {
                            digest = "[为爱牵线]";
                        }
                        break;
                    }
                    case ChatConstant.MSG_PHONE: {
                        String chatType;
                        String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
                        if (TextUtils.isEmpty(stringAttribute)){
                            Map<String, String> params = messageBody.getParams();
                            chatType = params.get("chatType");
                        }else {
                            CallFinishRewardBean callFinishRewardBean = GsonUtils.fromJson(stringAttribute, CallFinishRewardBean.class);
                            chatType = callFinishRewardBean.getChatType();
                        }
                        if (Objects.equals(chatType, "0")) {
                            digest = "[语音通话]";
                        } else {
                            digest = "[视频通话]";
                        }
                        break;
                    }
                    case ChatConstant.MSG_ADD_FRIEND:
                        Map<String, String> params = messageBody.getParams();
                        String targetNickName = params.get("targetNickName");
                        String isApplySuccess = params.get("isApplySuccess");
                        if ("0".equals(isApplySuccess)){
                            digest = "[添加好友提示]";
                        }else {
                            String fromNickName = params.get("fromNickName");
                            String showName ;
                            String from = message.getFrom();
                            if (AppCacheManager.INSTANCE.getUserId().equals(from)){
                                showName = targetNickName;
                            }else {
                                showName = fromNickName;
                            }
                            digest = "你与" + showName + "已成为好友，开始聊天吧～";
                        }

                        break;
                    case ChatConstant.MSG_GROUP_NEW:
                        digest = "[新用户入群]";
                        break;
                    case ChatConstant.MSG_GROUP_RED_PACKET:
                        digest = "[收到一个红包]";
                        break;
                    case ChatConstant.MSG_GROUP_CENTER_TIPS: {
                        String msg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
                        GroupCenterTipMsgInfo recordInfo = GsonUtils.fromJson(msg, GroupCenterTipMsgInfo.class);
                        String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "");
                        BaseUserInfo userInfo = GsonUtils.fromJson(stringAttribute, BaseUserInfo.class);
                        String content = recordInfo.getContent();
                        if (recordInfo.getType() == GroupCenterTipMsgInfo.TYPE_RECEIVE_RED_PACKET) {
                            if (message.direct() == EMMessage.Direct.SEND) {
                                digest = "你领取了" + content + "的红包";
                            } else {
                                digest = userInfo.getNickName() + "领取了你的红包";
                            }
                        } else {
                            digest = content;
                        }
                        break;
                    }
                    case ChatConstant.MSG_GROUP_INVITE: {
                        String msg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
                        GroupInviteRecord recordInfo = GsonUtils.fromJson(msg, GroupInviteRecord.class);
                        if (recordInfo.isJoinSuccess()) {
                            if (message.direct() == EMMessage.Direct.SEND) {
                                digest = "您已加入群聊:" + recordInfo.getGroupName();
                            } else {
                                EaseUser user = EaseUserUtils.getUserInfo(message.getFrom());
                                String nickName;
                                if (user != null) {
                                    nickName = user.getNickname();
                                } else {
                                    nickName = message.getFrom();
                                }
                                digest = nickName + "已加入群聊:" + recordInfo.getGroupName();
                            }
                        } else {
                            if (message.direct() == EMMessage.Direct.SEND) {
                                digest = "您已邀请对方加入群聊，请等待对方同意";
                            } else {
                                digest = recordInfo.getContent();
                            }
                        }
                        break;
                    }
                    case ChatConstant.MSG_CUSTOM_GIF_EMOJI:
                        digest = "[动画表情]";
                        break;
                    case ChatConstant.MSG_CUSTOM_EMOJI:
                        digest = "[表情]";
                        break;
                    default:
                        digest = "[系统消息]";
                        break;
                }

                break;
            case TXT:
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                if (txtBody != null) {
                    String txtBodyMessage = txtBody.getMessage();
                    String stringAttribute = message.getStringAttribute(ChatConstant.WELCOME_CONTENT, "");
                    if (!TextUtils.isEmpty(stringAttribute)) {
                        digest = String.format("@%s %s", txtBodyMessage, stringAttribute);
                    } else {
                        String aTeUserInfo = message.getStringAttribute(ChatConstant.ATE_USER_INFO, "");
                        if (!TextUtils.isEmpty(aTeUserInfo)) {
                            //群聊@用户 数据绑定
                            Spannable span = EaseSmileUtils.getSmiledText(context, txtBodyMessage);
                            GroupChatUserInfo ateUserInfo = GsonUtils.fromJson(aTeUserInfo, GroupChatUserInfo.class);
                            String fromUserInfoStr = message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "");
                            if (!TextUtils.isEmpty(fromUserInfoStr)) {
                                if (AppCacheManager.INSTANCE.getUserId().equals(String.valueOf(ateUserInfo.getUserId()))) {
                                    digest = "@你 " + span;
                                } else {
                                    digest = "@" + ateUserInfo.getNickName() + " " + span;
                                }
                            } else {
                                digest = "@" + ateUserInfo.getNickName() + " " + span;
                            }
                        } else {
                            if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                                digest = getString(context, R.string.voice_call) + txtBodyMessage;
                            } else if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                                digest = getString(context, R.string.video_call) + txtBodyMessage;
                            } else if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                                if (!TextUtils.isEmpty(txtBodyMessage)) {
                                    digest = txtBodyMessage;
                                } else {
                                    digest = getString(context, R.string.dynamic_expression);
                                }
                            } else {
                                digest = txtBodyMessage;
                            }
                        }

                    }

                }


                break;
            case FILE:
                digest = getString(context, R.string.file);
                break;
            default:
                EMLog.e(TAG, "error, unknow type");
                return "";
        }
        Log.e("TAG", "message text = " + digest);
        return replaceLineString(digest);
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    /**
     * get top context
     *
     * @param context
     * @return
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null) return runningTaskInfos.get(0).topActivity.getClassName();
        else return "";
    }

    /**
     * set initial letter of according user's nickname( username if no nickname)
     *
     * @param user
     */
    public static void setUserInitialLetter(EaseUser user) {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;

        final class GetInitialLetter {
            String getLetter(String name) {
                if (TextUtils.isEmpty(name)) {
                    return DefaultLetter;
                }
                char char0 = name.toLowerCase().charAt(0);
                if (Character.isDigit(char0)) {
                    return DefaultLetter;
                }
                String pinyin = HanziToPinyin.getPinyin(name);
                if (!TextUtils.isEmpty(pinyin)) {
                    String letter = pinyin.substring(0, 1).toUpperCase();
                    char c = letter.charAt(0);
                    if (c < 'A' || c > 'Z') {
                        return DefaultLetter;
                    }
                    return letter;
                }
                return DefaultLetter;
            }
        }

        if (!TextUtils.isEmpty(user.getNickname())) {
            letter = new GetInitialLetter().getLetter(user.getNickname());
            user.setInitialLetter(letter);
            return;
        }
        if (letter.equals(DefaultLetter) && !TextUtils.isEmpty(user.getUsername())) {
            letter = new GetInitialLetter().getLetter(user.getUsername());
        }
        user.setInitialLetter(letter);
    }

    /**
     * change the chat type to EMConversationType
     *
     * @param chatType
     * @return
     */
    public static EMConversationType getConversationType(int chatType) {
        if (chatType == EaseConstant.CHATTYPE_SINGLE) {
            return EMConversationType.Chat;
        } else if (chatType == EaseConstant.CHATTYPE_GROUP) {
            return EMConversationType.GroupChat;
        } else {
            return EMConversationType.ChatRoom;
        }
    }

    /**
     * get chat type by conversation type
     *
     * @param conversation
     * @return
     */
    public static int getChatType(EMConversation conversation) {
        if (conversation.isGroup()) {
            if (conversation.getType() == EMConversationType.ChatRoom) {
                return EaseConstant.CHATTYPE_CHATROOM;
            } else {
                return EaseConstant.CHATTYPE_GROUP;
            }
        } else {
            return EaseConstant.CHATTYPE_SINGLE;
        }
    }

    /**
     * \~chinese
     * 判断是否是免打扰的消息,如果是app中应该不要给用户提示新消息
     *
     * @param message return
     *                <p>
     *                \~english
     *                check if the message is kind of slient message, if that's it, app should not play tone or vibrate
     * @param message
     * @return
     */
    public static boolean isSilentMessage(EMMessage message) {
        return message.getBooleanAttribute("em_ignore_notification", false);
    }

    private static class GetInitialLetter {
        private String defaultLetter = "#";

        /**
         * 获取首字母
         *
         * @param name
         * @return
         */
        public String getLetter(String name) {
            if (TextUtils.isEmpty(name)) {
                return defaultLetter;
            }
            char char0 = name.toLowerCase().charAt(0);
            if (Character.isDigit(char0)) {
                return defaultLetter;
            }
            String pinyin = HanziToPinyin.getPinyin(name);
            if (!TextUtils.isEmpty(pinyin)) {
                String letter = pinyin.substring(0, 1).toUpperCase();
                char c = letter.charAt(0);
                if (c < 'A' || c > 'Z') {
                    return defaultLetter;
                }
                return letter;
            }
            return defaultLetter;
        }
    }

}
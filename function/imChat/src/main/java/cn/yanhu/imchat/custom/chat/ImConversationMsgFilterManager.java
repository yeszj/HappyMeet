package cn.yanhu.imchat.custom.chat;

import android.text.TextUtils;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;

import java.util.List;
import java.util.Map;

import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.commonres.manager.AppCacheManager;

/**
 * @author: zhengjun
 * created: 2024/6/13
 * desc:会话列表筛选缘分和系统消息
 */
public class ImConversationMsgFilterManager {
    /**
     * 筛选 系统消息和服务端牵线发送的消息不显示
     */
    public static EMMessage filterSendMsg(EMConversation item, EMMessage lastMessage) {
        try {
            int loveString = lastMessage.getIntAttribute("loveString", -1);
            //最后一条是为爱牵线系统自动发的文本消息 说明没有回复
            if (loveString == 1) {
                if (lastMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                    //如果为牵线消息没有回复  判断最近10条消息 如果是自己发的 则会话列表中显示上一条消息,如果10条都是自己发的 则默认显示最新一条消息
                    return showFinalMessage(item);
                }
            } else if (isLoveQianXianMsg(lastMessage)) {
                EMCustomMessageBody messageBody = (EMCustomMessageBody) lastMessage.getBody();
                Map<String, String> params = messageBody.getParams();
                String content = params.get("content");
                if (!TextUtils.isEmpty(content)) {
                    if (content.contains("为爱牵线")) {
                        //如果最后一条是为爱牵线说明没有回复 则筛选不显示
                        return showFinalMessage(item);
                    } else {
                        //如果是缘分牵线 筛选发送不显示
                        if (lastMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                            return showFinalMessage(item);
                        } else {
                            return filterSystemMsg(item, lastMessage);
                        }
                    }
                } else {
                    return filterSystemMsg(item, lastMessage);
                }

            } else {
                //判断最近10条消息 如果是系统消息 则会话列表中显示上一条消息,如果10条都是系统消息 则默认显示最新一条系统消息
                return filterSystemMsg(item, lastMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static EMMessage filterSystemMsg(EMConversation item, EMMessage lastMessage) {
        if (isCustomSystemMsg(lastMessage)) {
            List<EMMessage> allMessages = item.searchMsgFromDB(System.currentTimeMillis(), 10, EMConversation.EMSearchDirection.UP);
            for (int i = allMessages.size() - 1; i >= 0; i--) {
                EMMessage emMessage = allMessages.get(i);
                if (!isCustomSystemMsg(emMessage)) {
                    return emMessage;
                }
            }
        }
        return null;
    }

    private static EMMessage showFinalMessage(EMConversation item) {
        List<EMMessage> allMessages = item.searchMsgFromDB(System.currentTimeMillis(), 10, EMConversation.EMSearchDirection.UP);
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            EMMessage emMessage = allMessages.get(i);
            int loveString2 = emMessage.getIntAttribute("loveString", -1);
            if (loveString2 != 1 || !emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                if (emMessage.getType() == EMMessage.Type.CUSTOM) {
                    EMCustomMessageBody messageBody = (EMCustomMessageBody) emMessage.getBody();
                    String event = messageBody.event();
                    if (!ChatConstant.MSG_QIANXIAN.equals(event)) {
                        return emMessage;
                    }
                } else {
                    return emMessage;
                }
            }
            if (i == 0) {
                return emMessage;
            }
        }
        return null;
    }

    private static boolean isCustomSystemMsg(EMMessage emMessage) {
        EMMessage.Type type = emMessage.getType();
        if (type == EMMessage.Type.CUSTOM) {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) emMessage.getBody();
            String event = messageBody.event();
            return ChatConstant.MSG_ALERT.equals(event);
        }
        return false;
    }

    public static boolean isLoveQianXianMsg(EMMessage emMessage) {
        EMMessage.Type type = emMessage.getType();
        if (type == EMMessage.Type.CUSTOM) {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) emMessage.getBody();
            String event = messageBody.event();
            return ChatConstant.MSG_QIANXIAN.equals(event);
        }
        return false;
    }

    public static boolean isForLoveMsg(EMMessage emMessage) {
        EMMessage.Type type = emMessage.getType();
        if (type == EMMessage.Type.CUSTOM) {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) emMessage.getBody();
            String event = messageBody.event();
            boolean isQianXian = ChatConstant.MSG_QIANXIAN.equals(event);
            if (isQianXian){
                Map<String, String> params = messageBody.getParams();
                String content = params.get("content");
                if (!TextUtils.isEmpty(content)){
                    return content.contains("为爱牵线");
                }
            }
        }
        return false;
    }

    public static boolean isLoveStringMsg(EMMessage emMessage) {
        int loveString = emMessage.getIntAttribute("loveString", -1);
        return loveString == 1;
    }
}

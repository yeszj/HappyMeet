package cn.yanhu.imchat.custom.chat;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.conversation.presenter.EaseConversationPresenterImpl;

import java.util.Collections;
import java.util.List;

public class CustomEaseConversationPresenterImpl extends EaseConversationPresenterImpl {
    @Override
    protected void sortByTimestamp(List<EaseConversationInfo> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        Collections.sort(list, (o1, o2) -> {
            EMConversation item = (EMConversation) o1.getInfo();
            EMConversation item2 = (EMConversation) o2.getInfo();
            if (item!=null){
                EMMessage emMessage = ImConversationMsgFilterManager.filterSendMsg(item, item.getLastMessage());
                if (emMessage!=null){
                    o1.setTimestamp(emMessage.getMsgTime());
                }
            }
            if (item2!=null){
                EMMessage emMessage2 = ImConversationMsgFilterManager.filterSendMsg(item2, item2.getLastMessage());
                if (emMessage2!=null){
                    o2.setTimestamp(emMessage2.getMsgTime());
                }
            }
            return Long.compare(o2.getTimestamp(), o1.getTimestamp());
        });
    }
}


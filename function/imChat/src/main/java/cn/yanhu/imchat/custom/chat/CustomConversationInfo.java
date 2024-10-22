package cn.yanhu.imchat.custom.chat;

import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;

import cn.yanhu.imchat.bean.CacheConversationInfo;
import cn.yanhu.imchat.bean.ConversationFinalMessageInfo;
import cn.yanhu.commonres.bean.UserDetailInfo;

/**
 * 做为会话列表的条目对象
 */
public class CustomConversationInfo extends EaseConversationInfo {

    private UserDetailInfo dataBean;

    private ConversationFinalMessageInfo finalMessage;

    private CacheConversationInfo cacheConversationInfo;

    private boolean isClearUnReadCount = false;

    public boolean isClearUnReadCount() {
        return isClearUnReadCount;
    }

    public void setClearUnReadCount(boolean clearUnReadCount) {
        isClearUnReadCount = clearUnReadCount;
    }

    private boolean isLoadCache = false;

    public boolean isLoadCache() {
        return isLoadCache;
    }

    public void setLoadCache(boolean loadCache) {
        isLoadCache = loadCache;
    }

    public CacheConversationInfo getCacheConversationInfo() {
        return cacheConversationInfo;
    }

    public void setCacheConversationInfo(CacheConversationInfo cacheConversationInfo) {
        this.cacheConversationInfo = cacheConversationInfo;
    }

    public ConversationFinalMessageInfo getFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(ConversationFinalMessageInfo finalMessage) {
        this.finalMessage = finalMessage;
    }

    public UserDetailInfo getDataBean() {
        return dataBean;
    }

    public void setDataBean(UserDetailInfo dataBean) {
        this.dataBean = dataBean;
    }
}


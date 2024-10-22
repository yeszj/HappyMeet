package cn.yanhu.imchat.custom.chat;

import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;

public class CustomEaseConversationInfo extends EaseConversationInfo {

    /**
     * 用户id
     */
    private long userId;
    /**
     * 亲密度值
     */
    private long intimacy;
    /**
     * true: 真人 false:不是真人
     */
    private boolean isAuth;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 贵族等级:0表示不是贵族,大于0表示是贵族
     */
    private long nobleLevel;
    /**
     * 在线状态 0:在线 1：刚刚在线 -1：不在线
     */
    private long onlineStatus;
    /**
     * 用户头像
     */
    private String portrait;

    public long getIntimacy() {
        return intimacy;
    }

    public void setIntimacy(long value) {
        this.intimacy = value;
    }

    public boolean getIsAuth() {
        return isAuth;
    }

    public void setIsAuth(boolean value) {
        this.isAuth = value;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String value) {
        this.nickName = value;
    }

    public long getNobleLevel() {
        return nobleLevel;
    }

    public void setNobleLevel(long value) {
        this.nobleLevel = value;
    }

    public long getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(long value) {
        this.onlineStatus = value;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String value) {
        this.portrait = value;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long value) {
        this.userId = value;
    }
}

package cn.yanhu.agora.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @author: zhengjun
 * created: 2025/1/22
 * desc:
 */
public class InviteSeatRecord extends LitePalSupport {
    private long id;
    private String userId;
    private long inviteTime;

    public InviteSeatRecord() {
    }

    public InviteSeatRecord(String userId, long inviteTime) {
        this.userId = userId;
        this.inviteTime = inviteTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getInviteTime() {
        return inviteTime;
    }

    public void setInviteTime(long inviteTime) {
        this.inviteTime = inviteTime;
    }
}

package cn.yanhu.agora.bean;

import cn.yanhu.commonres.bean.BaseUserInfo;

/**
 * @author: zhengjun
 * created: 2024/9/13
 * desc:
 */
public class PkInviteMsgInfo extends BaseUserInfo {
    private String roomId;
    private boolean isRandomInvite;

    public boolean isRandomInvite() {
        return isRandomInvite;
    }

    public void setRandomInvite(boolean randomInvite) {
        isRandomInvite = randomInvite;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

package cn.yanhu.imchat.bean.request;

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
public class GetChatUsersRequest {
    /**
     * userIds : string
     */

    private String uIds;

    public String getUserIds() {
        return uIds;
    }

    public void setUserIds(String userIds) {
        this.uIds = userIds;
    }
}

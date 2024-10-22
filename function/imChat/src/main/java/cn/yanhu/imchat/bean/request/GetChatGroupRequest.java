package cn.yanhu.imchat.bean.request;

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
public class GetChatGroupRequest {
    /**
     * userIds : string
     */

    private String groupIds;

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }
}

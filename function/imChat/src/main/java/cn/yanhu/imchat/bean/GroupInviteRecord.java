package cn.yanhu.imchat.bean;

/**
 * @author: zhengjun
 * created: 2024/1/15
 * desc:
 */
public class GroupInviteRecord {
    private String id;
    private String content;

    private String groupId;
    private String groupName;

    private boolean isJoinSuccess;

    public boolean isJoinSuccess() {
        return isJoinSuccess;
    }

    public void setJoinSuccess(boolean joinSuccess) {
        isJoinSuccess = joinSuccess;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

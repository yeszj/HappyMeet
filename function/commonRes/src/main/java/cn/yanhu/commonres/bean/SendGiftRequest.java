package cn.yanhu.commonres.bean;

public class SendGiftRequest {
    public static final int SOURCE_LIVE_ROOM= 0;//
    public static final int SOURCE_CHAT = 1;//私聊
    public static final int SOURCE_CALL = 2;//一对一通话

    /**
     * roomId : 0
     * num : 0
     * giftId : 0
     * toUid : string
     * source : 0:为房间 1:一对一聊天 2:一对一语音 3:一对一通话
     * callId : 一对一通话ID
     */

    private String roomId;
    private int num;
    private int giftId;
    private String toUid;
    private Integer source;
    private Integer callId;

    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getCallId() {
        return callId;
    }

    public void setCallId(Integer callId) {
        this.callId = callId;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }
}

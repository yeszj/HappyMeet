package cn.yanhu.commonres.bean;

import java.io.Serializable;

/**
 * @author: zhengjun
 * created: 2024/9/20
 * desc:
 */
public class RoomPkEnterInfo extends PkRoomResultInfo implements Serializable {
    public RoomPkEnterInfo(int roomPkValue, int otherRoomPkValue, int countDownTime) {
        super(roomPkValue, otherRoomPkValue,countDownTime);
    }
    int pkStatus;//0 pk进行中 1本 轮结束 显示继续pk 2已邀请
    String pkRoomId;
    String pkUid;
    int pkTime;
    int praiseCount;
    int pkId;
    String nowRoomPortrait;
    String otherRoomPortrait;
    String nowRoomOwnerUserId;

    public String getPkUid() {
        return pkUid;
    }

    public void setPkUid(String pkUid) {
        this.pkUid = pkUid;
    }

    public int getPkTime() {
        return pkTime;
    }

    public void setPkTime(int pkTime) {
        this.pkTime = pkTime;
    }

    public String getNowRoomOwnerUserId() {
        return nowRoomOwnerUserId;
    }

    public void setNowRoomOwnerUserId(String nowRoomOwnerUserId) {
        this.nowRoomOwnerUserId = nowRoomOwnerUserId;
    }

    public String getNowRoomPortrait() {
        return nowRoomPortrait;
    }

    public void setNowRoomPortrait(String nowRoomPortrait) {
        this.nowRoomPortrait = nowRoomPortrait;
    }

    public String getOtherRoomPortrait() {
        return otherRoomPortrait;
    }

    public void setOtherRoomPortrait(String otherRoomPortrait) {
        this.otherRoomPortrait = otherRoomPortrait;
    }

    public int getPkId() {
        return pkId;
    }

    public void setPkId(int pkId) {
        this.pkId = pkId;
    }

    public int getPkState() {
        return pkStatus;
    }

    public void setPkState(int pkState) {
        this.pkStatus = pkState;
    }


    public String getPkRoomId() {
        return pkRoomId;
    }

    public void setPkRoomId(String pkRoomId) {
        this.pkRoomId = pkRoomId;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
    }
}

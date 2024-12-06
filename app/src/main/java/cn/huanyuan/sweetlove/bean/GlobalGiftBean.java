package cn.huanyuan.sweetlove.bean;

public class GlobalGiftBean {

    /**
     * giftIcon : http://xindaiguanjia-app-dev-new.oss-cn-hangzhou.aliyuncs.com/file/328329016386188557UUUUUUUU.png
     * fromUserPortrait : https://xindaiguanjia-app-prod-new.oss-cn-hangzhou.aliyuncs.com/loveplayFile/defaultPortrait_loveplay_male.png
     * fromUserId : 1
     * fromUserNickName : 用户ID1
     * giftName : h嗨嗨嗨嗨
     * giftPrice : 100
     * toUserNickName : 用户ID4
     * toUserId : 4
     * toUserPortrait : http://xindaiguanjia-app-dev-new.oss-cn-hangzhou.aliyuncs.com/file/267247826686120022UUUUUUUU.jpg
     */

    private String giftIcon;
    private String giftName;
    private String fromUserPortrait;
    private int fromUserId;
    private String fromUserNickName;
    private int giftPrice;
    private String toUserNickName;
    private int toUserId;
    private String toUserPortrait;

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public String getFromUserPortrait() {
        return fromUserPortrait;
    }

    public void setFromUserPortrait(String fromUserPortrait) {
        this.fromUserPortrait = fromUserPortrait;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserNickName() {
        return fromUserNickName;
    }

    public void setFromUserNickName(String fromUserNickName) {
        this.fromUserNickName = fromUserNickName;
    }

    public int getGiftPrice() {
        return giftPrice;
    }

    public void setGiftPrice(int giftPrice) {
        this.giftPrice = giftPrice;
    }

    public String getToUserNickName() {
        return toUserNickName;
    }

    public void setToUserNickName(String toUserNickName) {
        this.toUserNickName = toUserNickName;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserPortrait() {
        return toUserPortrait;
    }

    public void setToUserPortrait(String toUserPortrait) {
        this.toUserPortrait = toUserPortrait;
    }
}

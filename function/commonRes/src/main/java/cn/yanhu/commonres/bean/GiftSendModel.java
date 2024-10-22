package cn.yanhu.commonres.bean;

public class GiftSendModel {
    private String nickname;
    private String portrait;
    private String nobleFrame;
    private String sig;
    private String giftIcon;
    private Integer giftCount;

    public GiftSendModel(String nickname, String portrait, String sig, String giftIcon, Integer giftCount) {
        this.nickname = nickname;
        this.portrait = portrait;
        this.sig = sig;
        this.giftIcon = giftIcon;
        this.giftCount = giftCount;
    }

    public GiftSendModel(String nickname, String portrait, String nobleFrame, String sig, String giftIcon, Integer giftCount) {
        this.nickname = nickname;
        this.portrait = portrait;
        this.nobleFrame = nobleFrame;
        this.sig = sig;
        this.giftIcon = giftIcon;
        this.giftCount = giftCount;
    }

    public String getNobleFrame() {
        return nobleFrame;
    }

    public void setNobleFrame(String nobleFrame) {
        this.nobleFrame = nobleFrame;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public Integer getGiftCount() {
        return giftCount;
    }

    public void setGiftCount(Integer giftCount) {
        this.giftCount = giftCount;
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }
}

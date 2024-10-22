package cn.yanhu.commonres.bean;


import java.io.Serializable;
import java.util.Objects;

/**
 * @author gyn
 * @date 2021/10/9
 */
public class GiftInfo implements Serializable {
    private String name;
    private int price;
    private boolean allBroadcast;
    private String giftIcon;
    private String svga;
    private int id;
    private int sendNumber;
    private String tagIcon;

    public String getSvga() {
        return svga;
    }

    public void setSvga(String svga) {
        this.svga = svga;
    }

    public String getTagIcon() {
        return tagIcon;
    }

    public void setTagIcon(String tagIcon) {
        this.tagIcon = tagIcon;
    }

    public int getSendNumber() {
        return sendNumber;
    }

    public void setSendNumber(int sendNumber) {
        this.sendNumber = sendNumber;
    }


    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isAllBroadcast() {
        return allBroadcast;
    }

    public void setAllBroadcast(boolean allBroadcast) {
        this.allBroadcast = allBroadcast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GiftInfo)) return false;
        GiftInfo gift = (GiftInfo) o;
        return getId() == gift.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

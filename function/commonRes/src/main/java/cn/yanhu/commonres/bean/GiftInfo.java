package cn.yanhu.commonres.bean;


import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author gyn
 * @date 2021/10/9
 */
public class GiftInfo implements Serializable {
    public static final int TYPE_SONG = 11;//点歌礼物
    public static final int TYPE_RANDOM_BOX= 10;//随机礼物
    private String name;
    private int price;
    private boolean allBroadcast;
    private String giftIcon;
    private String svga;
    private int id;
    private int sendNumber;
    private String tagIcon;

    private int type;

    private String randomBoxGiftInfo;

    private int clickedCount;

    public int getClickedCount() {
        return clickedCount;
    }

    public void setClickedCount(int clickedCount) {
        this.clickedCount = clickedCount;
    }

    public String getRandomBoxGiftInfo() {
        return randomBoxGiftInfo;
    }

    public GiftInfo getRandomBoxGiftBean(){
        if(!TextUtils.isEmpty(randomBoxGiftInfo)){
            return GsonUtils.fromJson(randomBoxGiftInfo,GiftInfo.class);
        }
        return null;
    }

    public void setRandomBoxGiftInfo(String randomBoxGiftInfo) {
        this.randomBoxGiftInfo = randomBoxGiftInfo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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

package cn.yanhu.commonres.bean;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import cn.yanhu.commonres.BR;

/**
 * @author: zhengjun
 * created: 2023/12/18
 * desc:
 */
@Entity
public class BaseUserInfo extends BaseObservable implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String portrait;
    private String nickName;
    private int gender;
    private int age;
    private String province;

    private String city;
    private String hometown;

    private boolean isSelect;

    private String avatarFrame;
    private int level;

    private String leveIcon;

    private String description;

    private int onlineStatus;


    public boolean isOnline(){
        return onlineStatus == 0;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLeveIcon() {
        return leveIcon;
    }

    public void setLeveIcon(String leveIcon) {
        this.leveIcon = leveIcon;
    }

    public String getAvatarFrame() {
        return avatarFrame;
    }

    public void setAvatarFrame(String avatarFrame) {
        this.avatarFrame = avatarFrame;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getStrUserInfo() {
        return (gender == 1 ? "男" : "女") +" । "+ age + "岁" + (TextUtils.isEmpty(province) ? "" : " । " + province) + (TextUtils.isEmpty(hometown) ? "" : " । 老家" + hometown);
    }
    public String getStrNoGenderInfo() {
        return age + "岁" + (TextUtils.isEmpty(province) ? "" : " । " + province) + (TextUtils.isEmpty(hometown) ? "" : " । 老家" + hometown);
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Bindable
    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
        notifyPropertyChanged(BR.portrait);
    }

    @Bindable
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
        notifyPropertyChanged(BR.nickName);
    }

    @Bindable
    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
        notifyPropertyChanged(BR.gender);
    }

    @Bindable
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }

    @Bindable
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        notifyPropertyChanged(BR.city);
    }

    @Bindable
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
        notifyPropertyChanged(BR.province);
    }
}

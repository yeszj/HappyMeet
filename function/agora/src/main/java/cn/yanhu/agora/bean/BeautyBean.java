package cn.yanhu.agora.bean;

import androidx.databinding.BaseObservable;


public class BeautyBean extends BaseObservable {

    private int icon;
    private String name;
    private String key;
    private Integer value;

    public BeautyBean(int icon, String name, String key, Integer value) {
        this.icon = icon;
        this.name = name;
        this.key = key;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}

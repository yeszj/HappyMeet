package com.hyphenate.easeui.domain;

import com.hyphenate.easeui.domain.EaseEmojicon.Type;

import java.util.List;

/**
 * 一组表情所对应的实体类
 *
 */
public class EaseEmojiconGroupEntity {
    /**
     * 表情数据
     */
    private List<EaseEmojicon> emojiconList;
    /**
     * 图片
     */
    private int icon;

    private String iconPath;
    /**
     * 组名
     */
    private String name;
    /**
     * 表情类型
     */
    private Type type;

    private int emojiType;

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public EaseEmojiconGroupEntity(){}
    
    public EaseEmojiconGroupEntity(int icon, List<EaseEmojicon> emojiconList){
        this.icon = icon;
        this.emojiconList = emojiconList;
        type = Type.NORMAL;
    }
    
    public EaseEmojiconGroupEntity(int icon, List<EaseEmojicon> emojiconList, Type type){
        this.icon = icon;
        this.emojiconList = emojiconList;
        this.type = type;
    }
    
    public List<EaseEmojicon> getEmojiconList() {
        return emojiconList;
    }
    public void setEmojiconList(List<EaseEmojicon> emojiconList) {
        this.emojiconList = emojiconList;
    }
    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        if (emojiType == 1) {
            return Type.NORMAL;
        } else if (emojiType == 2) {
            return Type.BIG_EXPRESSION;
        }
        return Type.BIG_EXPRESSION;
    }

    public int getEmojiType() {
        return emojiType;
    }

    public void setEmojiType(int emojiType) {
        this.emojiType = emojiType;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    
}

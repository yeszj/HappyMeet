package cn.yanhu.imchat.custom.chat;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

public interface CustomIChatMessageItemSet {
    /**
     * 设置默认头像
     * @param src
     */
    void setAvatarDefaultSrc(Drawable src);

    /**
     * 设置头像样式
     * @param shapeType
     */
    void setAvatarShapeType(int shapeType);

    /**
     * 是否展示昵称
     * @param showNickname
     */
    void showNickname(boolean showNickname);

    /**
     * 设置条目发送者的背景
     * @param bgDrawable
     */
    void setItemSenderBackground(Drawable bgDrawable);

    /**
     * 设置接收者的背景
     * @param bgDrawable
     */
    void setItemReceiverBackground(Drawable bgDrawable);

    /**
     * 设置文本消息字体大小
     * @param textSize
     */
    void setItemTextSize(int textSize);

    /**
     * 设置文本消息字体颜色
     * @param textColor
     */
    void setItemTextColor(@ColorInt int textColor);

    /**
     * 设置文本消息条目的最小高度
     * @param height
     */
    //void setItemMinHeight(int height);

    /**
     * 设置时间线文本大小
     * @param textSize
     */
    void setTimeTextSize(int textSize);

    /**
     * 设置时间线文本颜色
     * @param textColor
     */
    void setTimeTextColor(int textColor);

    /**
     * 设置时间线背景
     * @param bgDrawable
     */
    void setTimeBackground(Drawable bgDrawable);

    /**
     * 聊天列表条目的展示方式
     * @param type
     */
    void setItemShowType(CustomEaseChatMessageListLayout.ShowType type);
}

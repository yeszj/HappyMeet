package com.pcl.sdklib.bean;

/**
 * @author: witness
 * created: 2023/1/6
 * desc:
 */
public class PostBaiduAuthBean {
    private String image;
    private String score;
    private String errorMsg;
    private String realName;
    private String idCard;

    public PostBaiduAuthBean(String image, String score, String realName, String idCard) {
        this.image = image;
        this.score = score;
        this.realName = realName;
        this.idCard = idCard;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}

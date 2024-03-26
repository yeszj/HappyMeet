package com.pcl.sdklib.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
public class WechatPayData {
    /**
     * package : Sign=WXPay
     * appid : wx9459c27b64217d27
     * sign : 4465AF3AEA4CAE727BBE52E89136E038
     * partnerid : 1580754951
     * prepayid : wx031904431349072664daecd090c7440000
     * noncestr : Sdu3UtkxjwrdqbuHmUElVmY1KDDXXHWZ
     * timestamp : 1688382283
     */

    @SerializedName("package")
    private String packageX;
    private String appid;
    private String sign;
    private String partnerid;
    private String prepayid;
    private String noncestr;
    private String timestamp;

    public String getPackageX() {
        return packageX;
    }

    public void setPackageX(String packageX) {
        this.packageX = packageX;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

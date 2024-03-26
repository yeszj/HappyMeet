package com.pcl.sdklib.bean;

public class PayRequest {

    private Integer rechargeId;
    private Integer rechargeType;

    public PayRequest(Integer rechargeId, Integer rechargeType) {
        this.rechargeId = rechargeId;
        this.rechargeType = rechargeType;
    }

    public Integer getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(Integer rechargeType) {
        this.rechargeType = rechargeType;
    }

    public PayRequest(Integer rechargeId) {
        this.rechargeId = rechargeId;
    }

    public Integer getRechargeId() {
        return rechargeId;
    }

    public void setRechargeId(Integer rechargeId) {
        this.rechargeId = rechargeId;
    }
}

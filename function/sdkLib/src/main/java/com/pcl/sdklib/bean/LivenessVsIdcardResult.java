package com.pcl.sdklib.bean;

import com.baidu.idl.face.api.model.ResponseResult;

/**
 * @author: witness
 * created: 2023/2/7
 * desc:
 */
public class LivenessVsIdcardResult extends ResponseResult {

    private double score;
    private double faceliveness;
    private String idcardImage;
    private int riskLevel;
    private int verifyStatus;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getIdcardImage() {
        return idcardImage;
    }

    public void setIdcardImage(String idcardImage) {
        this.idcardImage = idcardImage;
    }

    public double getFaceliveness() {
        return faceliveness;
    }

    public void setFaceliveness(double faceliveness) {
        this.faceliveness = faceliveness;
    }

    public int getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(int verifyStatus) {
        this.verifyStatus = verifyStatus;
    }
}

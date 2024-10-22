package com.pcl.sdklib.sdk.baiduFace.config;

import android.text.TextUtils;

import com.baidu.idl.face.platform.LivenessTypeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与控制台交互的配置信息
 * Created by v_liujialu01 on 2020/8/10.
 */
public class ConsoleConfig {

    /* 动作活体类型 */
    private static final Map<String, LivenessTypeEnum> actionKVMap = new HashMap<>();

    static {
        actionKVMap.put("eye", LivenessTypeEnum.Eye);
        actionKVMap.put("mouth", LivenessTypeEnum.Mouth);
        actionKVMap.put("headRight", LivenessTypeEnum.HeadRight);
        actionKVMap.put("headLeft", LivenessTypeEnum.HeadLeft);
        actionKVMap.put("headUp", LivenessTypeEnum.HeadUp);
        actionKVMap.put("headDown", LivenessTypeEnum.HeadDown);
    }

    /* 随机*/
    private static final String ACTION_RANDOM = "random";

    private float illumination = 40;       // 最小光照阈值
    private float blur = 0.5f;             // 模糊阈值
    private boolean useOcr = false;                // 是否使用Ocr
    private float maxIllumination = 220;   // 最大光照阈值
    private float leftEyeOcclu = 0.8f;     // 左眼遮挡阈值
    private float rightEyeOcclu = 0.8f;    // 右眼遮挡阈值
    private float noseOcclu = 0.8f;        // 鼻子遮挡阈值
    private float mouthOcclu = 0.8f;       // 嘴巴遮挡阈值
    private float leftCheekOcclu = 0.8f;   // 左脸颊遮挡阈值
    private float rightCheekOcclu = 0.8f;  // 右脸颊遮挡阈值
    private float chinOcclu = 0.8f;        // 下巴遮挡阈值
    private int pitch = 20;                // 抬头低头阈值
    private int yaw = 18;                  // 向左向右转头阈值
    private int roll = 20;                 // 偏头阈值

    private boolean isFaceVerifyRandom = false;
    private final List<LivenessTypeEnum> actions = new ArrayList<>();

    /**
     * 场景信息：String类型，方案Id
     */
    private String planId = "";    // 是活体类型

    /**
     * 活体类型：0:炫彩活体、1:动作活体、2:静默活体
     */
    private int faceLiveType = 0;    // 是活体类型
    private int faceActionNum = 0;    // 是活体类型
    private double riskScore = 80f;             // 公安验证是否是同一人的阈值设定
    private float liveScore = 0.8f;             // 活体分数

    // 用于从控制台获取，作为入参传入公安验证接口
    private String onlineImageQuality;
    private String onlineLivenessQuality;

    public float getIllumination() {
        return illumination;
    }

    public float getBlur() {
        return blur;
    }

    public boolean isFaceVerifyRandom() {
        return isFaceVerifyRandom;
    }

    public List<LivenessTypeEnum> getActions() {
        return actions;
    }

    public String getOnlineImageQuality() {
        return onlineImageQuality;
    }

    public String getOnlineLivenessQuality() {
        return onlineLivenessQuality;
    }

    public String getPlanId() {
        return planId;
    }

    public int getFaceLiveType() {
        return faceLiveType;
    }

    public int getFaceActionNum() {
        return faceActionNum;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public float getLiveScore() {
        return liveScore;
    }

    public boolean getUseOcr() {
        return useOcr;
    }

    public float getMaxIllumination() {
        return maxIllumination;
    }

    public float getLeftEyeOcclu() {
        return leftEyeOcclu;
    }

    public float getRightEyeOcclu() {
        return rightEyeOcclu;
    }

    public float getNoseOcclu() {
        return noseOcclu;
    }

    public float getMouthOcclu() {
        return mouthOcclu;
    }

    public float getLeftCheekOcclu() {
        return leftCheekOcclu;
    }

    public float getRightCheekOcclu() {
        return rightCheekOcclu;
    }

    public float getChinOcclu() {
        return chinOcclu;
    }

    public int getPitch() {
        return pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public int getRoll() {
        return roll;
    }

    /**
     * 解析json文件的内容
     *
     * @param jsonObject JSON数据
     * @throws JSONException JSON异常
     */
    public void parseFromJSONObject(JSONObject jsonObject) throws Exception {
        // 获取version 字段
        String version = jsonObject.optString("version");
        if (TextUtils.isEmpty(version)) {
            return;
        }

        if ("3.0.0".equals(version)) {
            parseFromJSON(jsonObject);
        } else {
            throw new JSONException("初始配置读取失败, 版本号获取不正确");
        }
    }

    private void parseFromJSON(JSONObject jsonObject) throws Exception {
        JSONObject jsonImageQuality = jsonObject.optJSONObject("localImageQuality");
        JSONObject jsonQualityLevel = null;
        JSONObject jsonLoose = jsonImageQuality.optJSONObject("loose");
        JSONObject jsonNormal = jsonImageQuality.optJSONObject("normal");
        JSONObject jsonStrict = jsonImageQuality.optJSONObject("strict");
        if (jsonLoose != null && jsonLoose.length() > 0) {
            jsonQualityLevel = jsonLoose;
        } else if (jsonNormal != null && jsonNormal.length() > 0) {
            jsonQualityLevel = jsonNormal;
        } else if (jsonStrict != null && jsonStrict.length() > 0) {
            jsonQualityLevel = jsonStrict;
        } else {
            throw new JSONException("初始配置读取失败, localImageQuality json为空");
        }

        if (jsonQualityLevel != null) {
            illumination = (float) jsonQualityLevel.optDouble("minIllum");
            maxIllumination = (float) jsonQualityLevel.optDouble("maxIllum");
            blur = (float) jsonQualityLevel.optDouble("blur");
            leftEyeOcclu = (float) jsonQualityLevel.optDouble("leftEyeOcclusion");
            rightEyeOcclu = (float) jsonQualityLevel.optDouble("rightEyeOcclusion");
            noseOcclu = (float) jsonQualityLevel.optDouble("noseOcclusion");
            mouthOcclu = (float) jsonQualityLevel.optDouble("mouseOcclusion");
            leftCheekOcclu = (float) jsonQualityLevel.optDouble("leftContourOcclusion");
            rightCheekOcclu = (float) jsonQualityLevel.optDouble("rightContourOcclusion");
            chinOcclu = (float) jsonQualityLevel.optDouble("chinOcclusion");
            pitch = jsonQualityLevel.optInt("pitch");
            yaw = jsonQualityLevel.optInt("yaw");
            roll = jsonQualityLevel.optInt("roll");
        }

        // ocr
        int collection = jsonObject.optInt("collection");
        if (collection == 1) {
            useOcr = collection == 1;
        }

        // 动作随机
        String randomFlag = jsonObject.optString("faceVerifyActionOrder");
        if (ACTION_RANDOM.equals(randomFlag)) {
            isFaceVerifyRandom = true;
        }

        onlineImageQuality = jsonObject.optString("onlineImageQuality");
        onlineLivenessQuality = jsonObject.optString("onlineLivenessQuality");

        JSONArray ja = jsonObject.optJSONArray("faceVerifyAction");

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {
                String action = ja.getString(i);
                if (actionKVMap.get(action) == null) {
                    throw new JSONException("初始配置读取失败, JSON格式不正确");
                }
                actions.add(actionKVMap.get(action));
            }
        }

        planId = jsonObject.optString("planId");
        faceLiveType = jsonObject.optInt("faceLivenessType");
        faceActionNum = jsonObject.optInt("faceActionNum");
        riskScore = jsonObject.optDouble("policeThreshold");
        liveScore = new Double(jsonObject.optDouble("livenessThreshold")).floatValue();
    }
}

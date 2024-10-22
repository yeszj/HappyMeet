package com.pcl.sdklib.sdk.baiduFace;

import android.util.Log;

import com.baidu.idl.face.api.exception.FaceException;
import com.pcl.sdklib.bean.LivenessVsIdcardResult;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * @author: witness
 * created: 2023/2/7
 * desc:
 */
public class PoliceCheckResultParser implements Parser<LivenessVsIdcardResult> {

    @Override
    public LivenessVsIdcardResult parse(String json) throws FaceException {

        Log.i("PoliceCheckResultParser", "LivenessVsIdcardResult->" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceException error = new FaceException(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                if (error.getErrorCode() != 0) {
                    throw error;
                }
            }

            LivenessVsIdcardResult result = new LivenessVsIdcardResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);
            if (jsonObject.has("risk_level")) {
                int riskLevel = jsonObject.optInt("risk_level");
                result.setRiskLevel(riskLevel);
            }
            if (jsonObject.has("dec_image")){
                String dec_image = jsonObject.optString("dec_image");
                result.setIdcardImage(dec_image);
            }

            JSONObject resultObject = jsonObject.optJSONObject("result");
            if (resultObject != null) {
                if (resultObject.has("score")) {
                    double score = resultObject.optDouble("score");
                    result.setScore(score);
                }

                if (resultObject.has("face_liveness")) {
                    double liveness = resultObject.optDouble("face_liveness");
                    result.setFaceliveness(liveness);
                }

                if (resultObject.has("verify_status")) {
                    int verifyStatus = resultObject.optInt("verify_status");
                    result.setVerifyStatus(verifyStatus);
                }
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceException error = new FaceException(FaceException.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}

package com.pcl.sdklib.sdk.baiduFace

import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.utils.PermissionXUtils
import com.baidu.idl.face.api.exception.FaceException
import com.baidu.idl.face.api.manager.FaceConst
import com.baidu.idl.face.api.manager.FaceServiceManager
import com.baidu.idl.face.platform.FaceEnvironment
import com.baidu.idl.facelive.api.FaceLiveManager
import com.baidu.idl.facelive.api.entity.FaceLiveConfig
import com.baidu.idl.facelive.api.entity.LivenessValueModel
import com.blankj.utilcode.util.AppUtils
import com.pcl.sdklib.bean.BaiduPackBean
import com.pcl.sdklib.bean.LivenessVsIdcardResult
import com.pcl.sdklib.sdk.baiduFace.config.AuthConfigManager
import com.pcl.sdklib.sdk.baiduFace.config.ConsoleConfig
import com.pcl.sdklib.sdk.baiduFace.config.LicenseConfig

/**
 * @author: witness
 * created: 2023/1/5
 * desc:
 */
class BaiduFaceAuthUtils {
    private var consoleConfig: ConsoleConfig? = null
    private var onSubmitAuthListener: OnSubmitAuthListener? = null

    fun startBaiduFaceAuth(
        activity: FragmentActivity,
        bean: BaiduPackBean,
        onSubmitAuthListener: OnSubmitAuthListener
    ) {
        this.onSubmitAuthListener = onSubmitAuthListener
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            activity,
            permissions,
            "${AppUtils.getAppName()}想访问您的以下权限，用于实名认证",
            "您拒绝授权权限，无法开始实名认证服务",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    startBaiduSdk(activity, bean)
                }

                override fun onFail() {
                }

            })
    }

    private fun startBaiduSdk(activity: FragmentActivity?, bean: BaiduPackBean) {
        activity!!.runOnUiThread {
            DialogUtils.showLoading()
            consoleConfig = AuthConfigManager.getInstance(activity).config
            setFaceQualityConfig()
            // 初始化
            FaceServiceManager.getInstance().init(
                activity,
                LicenseConfig.licenseID,
                LicenseConfig.licenseFileName,
                LicenseConfig.keyName,
            ) { resultCode: Int, resultMsg: String ->
                Log.d(
                    "init callback", "resultCode:$resultCode,resultMsg:$resultMsg"
                )
                openCloudFaceService(activity, bean)
            }
        }
    }

    //仅活体检测
    private fun openCloudFaceService(activity: FragmentActivity, bean: BaiduPackBean) {
        val params: MutableMap<String, Any> = HashMap()
        // 必须携带access_token
        params["access_token"] = bean.accessToken
        // 开放平台控制台配置的方案Id
        params["plan_id"] = consoleConfig!!.planId
        // 证件类型，0：大陆 1：港澳 2：外国 3：定居国外中国护照
        // params.put("verify_type", cardType);
        // recogType为FaceRecognize 需要带姓名、证件号
        params["name"] = bean.realName
        params["id_card_number"] = bean.idCard
        /*
         * recogType为FaceRecognize可以携带以下参数
         * liveness_control 对应console.onlineLivenessQuality
         */params["quality_control"] = consoleConfig!!.onlineImageQuality
        params["liveness_control"] = consoleConfig!!.onlineLivenessQuality
        FaceServiceManager.getInstance().startFaceRecognize(
            activity, params
        ) { resultCode: Int, resultMap: Map<String, Any> ->
            activity.runOnUiThread {
                handleResult(
                    activity, resultCode, resultMap, bean
                )
            }
        }
    }

    private fun handleResult(
        activity: FragmentActivity,
        resultCode: Int,
        resultMap: Map<String, Any>,
        bean: BaiduPackBean,
    ) {
        if (resultCode == 0) {
            val data = resultMap[FaceConst.RESULT_JSON] as String?
            val parser = PoliceCheckResultParser()
            var result: LivenessVsIdcardResult? = null
            try {
                result = parser.parse(data)
                if (result == null) {
                    return
                }
                if (result.riskLevel == 1 || result.riskLevel == 2) {
                    val intent: Intent = Intent(
                        activity,
                        VerifyFailureActivity::class.java
                    )
                    // 风险级别 1 ：高危、2 ：嫌疑、3 ：普通、4 ：正常
                    intent.putExtra("risk_level", result.riskLevel)
                    activity.startActivity(intent)
                    return
                }
                val isPass = result.score >= consoleConfig!!.riskScore
                if (isPass) {
                    onSubmitAuthListener?.onAuthSuccess(result.idcardImage,result.score.toString())
                } else {
                    val intent = Intent(
                        activity,
                        VerifyFailureActivity::class.java
                    )
                    // 低分
                    intent.putExtra("err_code", -1)
                    intent.putExtra("verify_status", result.verifyStatus)
                    intent.putExtra("risk_level", result.riskLevel)
                    activity.startActivity(intent)
                }
            } catch (e: FaceException) {
                // 服务端错误
                val intent = Intent(
                    activity,
                    VerifyFailureActivity::class.java
                )
                intent.putExtra("err_code", e.errorCode)
                activity.startActivity(intent)
                e.printStackTrace()
            }
        } else {
            // SDK本地错误码
            onSubmitAuthListener?.onAuthFail()
            showToast(resultCode.toString() + ":" + resultMap["resultMsg"])
        }
    }


    private fun setFaceQualityConfig() {
        try {
            val faceLiveConfig = FaceLiveConfig()
            // faceUI默认展示结果页，此处必须设置为false
            faceLiveConfig.isShowResultView = false
            // 设置模糊度阈值
            faceLiveConfig.blurnessValue = consoleConfig!!.blur
            // 设置最小光照阈值（范围0-255）
            faceLiveConfig.brightnessValue = consoleConfig!!.illumination
            // 设置最大光照阈值（范围0-255）
            faceLiveConfig.brightnessMaxValue = consoleConfig!!.maxIllumination
            // 设置左眼遮挡阈值
            faceLiveConfig.occlusionLeftEyeValue = consoleConfig!!.leftEyeOcclu
            // 设置右眼遮挡阈值
            faceLiveConfig.occlusionRightEyeValue = consoleConfig!!.rightEyeOcclu
            // 设置鼻子遮挡阈值
            faceLiveConfig.occlusionNoseValue = consoleConfig!!.noseOcclu
            // 设置嘴巴遮挡阈值
            faceLiveConfig.occlusionMouthValue = consoleConfig!!.mouthOcclu
            // 设置左脸颊遮挡阈值
            faceLiveConfig.occlusionLeftContourValue = consoleConfig!!.leftCheekOcclu
            // 设置右脸颊遮挡阈值
            faceLiveConfig.occlusionRightContourValue = consoleConfig!!.rightCheekOcclu
            // 设置下巴遮挡阈值
            faceLiveConfig.occlusionChinValue = consoleConfig!!.chinOcclu
            // 设置人脸姿态Pitch阈值
            faceLiveConfig.headPitchValue = consoleConfig!!.pitch
            // 设置人脸姿态Yaw阈值
            faceLiveConfig.headYawValue = consoleConfig!!.yaw
            // 设置人脸姿态Roll阈值
            faceLiveConfig.headRollValue = consoleConfig!!.roll
            // 是否开启录制视频
            faceLiveConfig.isOpenRecord = false
            // 设置是否显示超时弹框
            faceLiveConfig.setIsShowTimeoutDialog(true)
            // 输出图片类型：0原图、1抠图
            faceLiveConfig.outputImageType = FaceEnvironment.VALUE_OUTPUT_IMAGE_TYPE
            // 设置活体类型相关
            setFaceLivenessConfig(faceLiveConfig)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFaceLivenessConfig(faceLiveConfig: FaceLiveConfig) {
        try {
            // 设置活体类型：炫彩活体、动作活体、静默活体

            // 配置活体动作集合、动作个数，活体阈值，无活体
            var livenessValueModel: LivenessValueModel? = null
            if (consoleConfig!!.faceLiveType == 0) {
                // 是否开启炫彩活体能力
                faceLiveConfig.setIsOpenColorLive(true)
                // 是否开启动作活体能力
                faceLiveConfig.setIsOpenActionLive(false)
                livenessValueModel = faceLiveConfig.livenessValueModel
                livenessValueModel.actionList.addAll(consoleConfig!!.actions)
                livenessValueModel.livenessScore = consoleConfig!!.liveScore
            } else if (consoleConfig!!.faceLiveType == 1) {
                // 是否开启炫彩活体能力
                faceLiveConfig.setIsOpenColorLive(false)
                // 是否开启动作活体能力
                faceLiveConfig.setIsOpenActionLive(false)
                livenessValueModel = faceLiveConfig.livenessValueModel
                livenessValueModel.actionList.addAll(consoleConfig!!.actions)
                livenessValueModel.actionRandomNumber = consoleConfig!!.faceActionNum
                livenessValueModel.livenessScore = consoleConfig!!.liveScore
            } else if (consoleConfig!!.faceLiveType == 2) {
                // 是否开启炫彩活体能力
                faceLiveConfig.setIsOpenColorLive(false)
                // 是否开启动作活体能力
                faceLiveConfig.setIsOpenActionLive(false)
                livenessValueModel = faceLiveConfig.livenessValueModel
                livenessValueModel.livenessScore = consoleConfig!!.liveScore
            } else if (consoleConfig!!.faceLiveType == 3) {
                // 是否开启炫彩活体能力
                faceLiveConfig.setIsOpenColorLive(true)
                // 是否开启动作活体能力
                faceLiveConfig.setIsOpenActionLive(false)
                livenessValueModel = faceLiveConfig.livenessValueModel
            }
            faceLiveConfig.livenessValueModel = livenessValueModel
            FaceLiveManager.getInstance().faceConfig = faceLiveConfig
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnSubmitAuthListener {
        fun onAuthSuccess(idCardImage: String,score:String)
        fun onAuthFail()
    }

    companion object {
        fun getInstance() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = BaiduFaceAuthUtils()
    }
}
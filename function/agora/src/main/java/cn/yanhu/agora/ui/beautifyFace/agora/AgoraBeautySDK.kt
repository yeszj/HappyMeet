package cn.yanhu.agora.ui.beautifyFace.agora

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import cn.happy.beautyface.bean.BeautyBgCache
import cn.happy.beautyface.bean.BeautyConfigCache
import cn.happy.beautyface.ui.FileUtils
import cn.yanhu.agora.manager.RtcEngineInit.mRtcEngine
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.FaceShapeAreaOptions
import io.agora.rtc2.video.FaceShapeBeautyOptions
import io.agora.rtc2.video.FilterEffectOptions
import io.agora.rtc2.video.MakeUpOptions
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VirtualBackgroundSource
import org.json.JSONException
import org.json.JSONObject

object AgoraBeautySDK {
    private const val TAG = "AgoraBeautySDK"
    var rtcEngine: RtcEngine? = null
    private var basicEnable = false
    private var filterEnable = false
    private var faceShapeEnable = false
    private var makeupEnable = false

    private var beauty_bundle_dir: String? = ""
    var isEnable = false

    // 美颜配置
    lateinit var beautyConfig: BeautyConfig
    var beautyConfigCache: BeautyConfigCache = getDefaultConfig()

    fun setDefaultConfig(context: Context) {
        beautyConfigCache = getDefaultConfig()
        AppCacheManager.beautyBg = ""
        beautyConfig = BeautyConfig()
        //  resetVirtualBackground(context)
        beautyConfig.resume()
    }

    fun getDefaultConfig(): BeautyConfigCache {
        return if (TextUtils.isEmpty(AppCacheManager.beautyDefaultConfig)) BeautyConfigCache() else GsonUtils.fromJson(
            AppCacheManager.beautyDefaultConfig, BeautyConfigCache::class.java
        )
    }

    fun isInitBeautyConfig(): Boolean {
        return ::beautyConfig.isInitialized
    }

    @JvmStatic
    fun initBeautySDK(context: Context, rtcEngine: RtcEngine, isInit: Boolean = true): Boolean {
        beauty_bundle_dir = FileUtils.copyFileAndUnzipFromAssets(
            context, "LocalResource.zip", context.externalCacheDir?.absolutePath
        )
        this.rtcEngine = rtcEngine
        if (!isInitBeautyConfig() || isInit) {
            val beautyConfigCacheInfo = AppCacheManager.beautyConfigCache
            if (!TextUtils.isEmpty(beautyConfigCacheInfo)) {
                beautyConfigCache = GsonUtils.fromJson<BeautyConfigCache>(
                    beautyConfigCacheInfo, BeautyConfigCache::class.java
                )
            }
            beautyConfig = BeautyConfig()
        }
        val ret = rtcEngine.enableExtension(
            "agora_video_filters_clear_vision",
            "clear_vision",
            true,
            Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
        )
        if (ret != Constants.ERR_OK) {
            logcom(
                TAG,
                "enableExtension failed: errorMsg:${RtcEngine.getErrorDescription(ret)},errorCode:$ret"
            )
            return false
        }
        // The private parameter is not supported, use VideoFrameObserver#getMirrorApplied instead
        // rtcEngine.setParameters("{\"rtc.camera_capture_mirror_mode\":0}")
        //  resetVirtualBackground(context)
        beautyConfig.resume()
        return true
    }

    @JvmStatic
    fun unInitBeautySDK() {
        rtcEngine?.setBeautyEffectOptions(false, beautyConfig.beautyOption)
        rtcEngine?.setFilterEffectOptions(false, beautyConfig.filterOption)
        rtcEngine?.setFaceShapeBeautyOptions(false, beautyConfig.faceShapeOption)
        val makeupObj = JSONObject()
        try {
            makeupObj.put("enable_mu", false)
            rtcEngine?.setExtensionProperty(
                "agora_video_filters_clear_vision",
                "clear_vision",
                "makeup_options",
                makeupObj.toString(),
                Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        rtcEngine?.enableExtension(
            "agora_video_filters_clear_vision",
            "clear_vision",
            false,
            Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
        )
        // The private parameter is not supported, use VideoFrameObserver#getMirrorApplied instead
        // rtcEngine?.setParameters("{\"rtc.camera_capture_mirror_mode\":2}")
        rtcEngine = null
        basicEnable = false
        filterEnable = false
        faceShapeEnable = false
        makeupEnable = false
        beautyConfig.reset()
    }

    fun enable(enable: Boolean) {
        isEnable = enable
        if (enable) {
            enableBasic(true)
            enableFilter(true)
            enableFaceShape(true)
            enableMakeup(beautyConfig.makeupOption.mMakeUpEnable)
        } else {
            enableBasic(false)
            enableFilter(false)
            enableFaceShape(false)
            enableMakeup(false)
        }
    }

    private fun enableBasic(enable: Boolean) {
        val rtc = rtcEngine ?: return
        rtc.setBeautyEffectOptions(enable, beautyConfig.beautyOption)
        beautyConfigCache.basicBeauty = enable
        this.basicEnable = enable
    }

    private fun enableFaceShape(enable: Boolean, force: Boolean = false) {
        val rtc = rtcEngine ?: return
        if (this.faceShapeEnable == enable && !force) return
        rtc.setFaceShapeBeautyOptions(enable, beautyConfig.faceShapeOption)
        beautyConfigCache.faceShape = enable
        this.faceShapeEnable = enable
    }

    private fun enableFilter(enable: Boolean) {
        val rtc = rtcEngine ?: return
        rtc.setFilterEffectOptions(enable, beautyConfig.filterOption)
        beautyConfigCache.filter = enable
        this.filterEnable = enable
    }

    private fun enableMakeup(enable: Boolean) {
        val rtc = rtcEngine ?: return
        val makeupObj = JSONObject()
        try {
            if (!enable) {
                beautyConfigCache.makeupType = 0
                makeupObj.put("enable_mu", false)
            } else {
                // 素材用本地还是下载
                makeupObj.put("resPath", "$beauty_bundle_dir/makeup")
                makeupObj.put("enable_mu", beautyConfig.makeupOption.mMakeUpEnable);
                makeupObj.put("browStyle", beautyConfig.makeupOption.mBrowType);
                makeupObj.put("browColor", beautyConfig.makeupOption.mBrowColor);
                makeupObj.put("browStrength", beautyConfig.makeupOption.mBrowStrength);
                makeupObj.put("lashStyle", beautyConfig.makeupOption.mLashType);
                makeupObj.put("lashColor", beautyConfig.makeupOption.mLashColor);
                makeupObj.put("lashStrength", beautyConfig.makeupOption.mLashStrength);
                makeupObj.put("shadowStyle", beautyConfig.makeupOption.mShadowType);
                makeupObj.put("shadowStrength", beautyConfig.makeupOption.mShadowStrength);
                makeupObj.put("pupilStyle", beautyConfig.makeupOption.mPupilType);
                makeupObj.put("pupilStrength", beautyConfig.makeupOption.mPupilStrength);
                makeupObj.put("blushStyle", beautyConfig.makeupOption.mBlushType);
                makeupObj.put("blushColor", beautyConfig.makeupOption.mBlushColor);
                makeupObj.put("blushStrength", beautyConfig.makeupOption.mBlushStrength);
                makeupObj.put("lipStyle", beautyConfig.makeupOption.mLipType);
                makeupObj.put("lipColor", beautyConfig.makeupOption.mLipColor);
                makeupObj.put("lipStrength", beautyConfig.makeupOption.mLipStrength);
            }
            rtc.setExtensionProperty(
                "agora_video_filters_clear_vision",
                "clear_vision",
                "makeup_options",
                makeupObj.toString(),
                Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        this.makeupEnable = enable
    }

    fun getFilerType(): FilterStyle {
        return try {
            FilterStyle.valueOf(beautyConfigCache.filterType)
        } catch (e: Exception) {
            e.printStackTrace()
            FilterStyle.None
        }
    }

    fun saveBeautyConfig(source: String? = "") {
        AppCacheManager.beautyConfigCache = GsonUtils.toJson(beautyConfigCache)
        saveVirtualBackgroundCache(source)
    }

    enum class FilterStyle(val value: Int) {
        None(0), NUANNAN(1), QISE(2), QIZHI(3), YUANQI(4)
    }

    class BeautyConfig {

        // 基础美颜配置
        internal val beautyOption = BeautyOptions()

        // 滤镜配置
        internal val filterOption = FilterEffectOptions()

        // 美型配置
        internal val faceShapeOption = FaceShapeBeautyOptions()

        // 美妆配置
        internal val makeupOption = MakeUpOptions()

        // 基础美颜
        var basicBeauty = beautyConfigCache.basicBeauty
            set(value) {
                field = value
                beautyConfigCache.basicBeauty = value
                enableBasic(value)
            }

        // 滤镜
        var filter = beautyConfigCache.filter
            set(value) {
                field = value
                beautyConfigCache.filter = value
                enableFilter(value)
            }

        // 磨皮程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始磨皮程度，默认值为 0.5。取值越大，磨皮程度越大。
        var smooth: Float = beautyConfigCache.smooth
            set(value) {
                field = value
                beautyConfigCache.smooth = value
                beautyOption.smoothnessLevel = value
                basicBeauty = true
            }
        var adjustContrast = beautyConfigCache.adjustContrast
            set(value) {
                field = value
                beautyConfigCache.adjustContrast = value
                beautyOption.lighteningContrastLevel = value
                basicBeauty = true
            }

        // 美白程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始亮度，默认值为 0.6。取值越大，美白程度越大。
        var whiten: Float = beautyConfigCache.whiten
            set(value) {
                field = value
                beautyConfigCache.whiten = value

                beautyOption.lighteningLevel = value
                basicBeauty = true
            }

        // 红润度，取值范围为 [0.0,1.0]，其中 0.0 表示原始红润度，默认值为 0.1。取值越大，红润程度越大。
        var redden = beautyConfigCache.redden
            set(value) {
                field = value
                beautyConfigCache.redden = value
                beautyOption.rednessLevel = value
                basicBeauty = true
            }

        var darkCircles = beautyConfigCache.darkCircles
            set(value) {
                field = value
                beautyConfigCache.darkCircles = value
                basicBeauty = true
                updateFaceBuffingOption("eye_pouch", value)
            }

        var nasolabialFolds = beautyConfigCache.nasolabialFolds
            set(value) {
                field = value
                beautyConfigCache.nasolabialFolds = value
                basicBeauty = true
                updateFaceBuffingOption("nasolabial_fold", value)
            }

        // 滤镜
        var filterType = getFilerType()
            set(value) {
                beautyConfigCache.filterType = value.name
                field = value
                when (value) {
                    FilterStyle.NUANNAN -> { // 原生
                        filterOption.path = "$beauty_bundle_dir/lut_png/nuannan.png"
                        filter = true
                    }

                    FilterStyle.QISE -> {
                        filterOption.path = "$beauty_bundle_dir/lut_png/qise.png"
                        filter = true
                    }

                    FilterStyle.QIZHI -> {
                        filterOption.path = "$beauty_bundle_dir/lut_png/qizhi.png"
                        filter = true
                    }

                    FilterStyle.YUANQI -> {
                        filterOption.path = "$beauty_bundle_dir/lut_png/yuanqi.png"
                        filter = true
                    }

                    else -> {
                        filter = false
                    }
                }
            }

        var filterStrength = beautyConfigCache.filterStrength
            set(value) {
                field = value
                beautyConfigCache.filterStrength = value
                filterOption.strength = value
                val rtc = rtcEngine ?: return
                rtc.setFilterEffectOptions(filterEnable, beautyConfig.filterOption)
            }

        // 锐化程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始锐度，默认值为 0.3。取值越大，锐化程度越大。
        var sharpen = beautyConfigCache.sharpen
            set(value) {
                field = value
                beautyConfigCache.sharpen = value
                beautyOption.sharpnessLevel = value
                basicBeauty = true
            }

        // 美型
        var faceShape = beautyConfigCache.faceShape
            set(value) {
                field = value
                beautyConfigCache.faceShape = value
                enableFaceShape(value)
            }

        // 大眼 对应修饰力度范围为 [0,100]，值越大，眼睛越大，预设值为 53。
        var enlargeEye = beautyConfigCache.enlargeEye
            set(value) {
                field = value
                beautyConfigCache.enlargeEye = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYESCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 下巴 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强，预设值为 -20。
        var chinLength = beautyConfigCache.chinLength
            set(value) {
                field = value
                beautyConfigCache.chinLength = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHIN, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦脸 对应修饰力度范围为 [0,100]，值越大瘦脸效果越强，预设值为 10。
        var thinFace = beautyConfigCache.thinFace
            set(value) {
                field = value
                beautyConfigCache.thinFace = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACECONTOUR, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦颧骨 对应修饰力度范围为 [0,100]，值越大颧骨越窄，预设值为 43。
        var shrinkCheekbone = beautyConfigCache.shrinkCheekbone
            set(value) {
                field = value
                beautyConfigCache.shrinkCheekbone = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEKBONE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        //长鼻 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强，预设值为 -10。
        var longNose = beautyConfigCache.longNose
            set(value) {
                field = value
                beautyConfigCache.longNose = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSELENGTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦鼻 对应修饰力度范围为 [-100,100]，正值为变宽，负值为变窄，绝对值越大修饰效果越强，预设值为 72。
        var narrowNose = beautyConfigCache.narrowNose
            set(value) {
                field = value
                beautyConfigCache.narrowNose = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEWIDTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 嘴型 对应修饰力度范围为 [-100,100]，正值为变大，负值为变小，绝对值越大修饰效果越强，预设值为 20。
        var mouthSize = beautyConfigCache.mouthSize
            set(value) {
                field = value
                beautyConfigCache.mouthSize = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHSCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 下颌骨 对应修饰力度范围为 [0,100]，值越大脸颊越窄，预设值为 50。
        var shrinkJawbone = beautyConfigCache.shrinkJawbone
            set(value) {
                field = value
                beautyConfigCache.shrinkJawbone = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEK, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 发际线 对应修饰力度范围为 [-100,100]，正值为调高，负值为调低，绝对值越大修饰效果越强，预设值为 50。
        var hairlineHeight = beautyConfigCache.hairlineHeight
            set(value) {
                field = value
                beautyConfigCache.hairlineHeight = value
                faceShape = true
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FOREHEAD, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 绅士脸
        var gentlemanFace = beautyConfigCache.gentlemanFace
            set(value) {
                field = value
                beautyConfigCache.gentlemanFace = value
                faceShapeOption.shapeStyle = FaceShapeBeautyOptions.FACE_SHAPE_BEAUTY_STYLE_MALE
                faceShapeOption.styleIntensity = value
                enableFaceShape(enable = true, force = true)
            }

        // 淑女脸
        var ladyFace = beautyConfigCache.ladyFace
            set(value) {
                field = value
                beautyConfigCache.ladyFace = value
                faceShapeOption.shapeStyle = FaceShapeBeautyOptions.FACE_SHAPE_BEAUTY_STYLE_FEMALE
                faceShapeOption.styleIntensity = value
                enableFaceShape(enable = true, force = true)
            }

        // 小头
        var headScale = beautyConfigCache.headScale
            set(value) {
                faceShape = true
                field = value
                beautyConfigCache.headScale = value
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_HEADSCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

//        // 长脸
//        var longFace = beautyConfigCache.longFace
//            set(value) {
//                faceShape = true
//                field = value
//                beautyConfigCache.longFace = value
//                val areaOption =
//                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACELENGTH, value);
//                rtcEngine?.setFaceShapeAreaOptions(areaOption)
//            }

        // 窄脸
        var narrowFace = beautyConfigCache.narrowFace
            set(value) {
                faceShape = true
                field = value
                beautyConfigCache.narrowFace = value
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACEWIDTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        //v脸
        var mandible = beautyConfigCache.mandible
            set(value) {
                faceShape = true
                field = value
                beautyConfigCache.mandible = value
                val areaOption =
                    FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MANDIBLE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 美妆素材
        var makeupType = beautyConfigCache.makeupType
            set(value) {
                field = value
                beautyConfigCache.makeupType = value
                when (value) {
                    1 -> { // 第1套美妆 混血
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 1
                        beautyConfig.makeupOption.mBrowColor = 2
                        beautyConfig.makeupOption.mLashType = 5
                        beautyConfig.makeupOption.mLashColor = 2
                        beautyConfig.makeupOption.mShadowType = 1
                        beautyConfig.makeupOption.mPupilType = 1
                        beautyConfig.makeupOption.mBlushType = 1
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 1
                        beautyConfig.makeupOption.mLipColor = 1
                        enableMakeup(true)
                    }

                    2 -> {  // 第2套美妆 精致
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 2
                        beautyConfig.makeupOption.mBrowColor = 2
                        beautyConfig.makeupOption.mLashType = 5
                        beautyConfig.makeupOption.mLashColor = 1
                        beautyConfig.makeupOption.mShadowType = 6
                        beautyConfig.makeupOption.mPupilType = 2
                        beautyConfig.makeupOption.mBlushType = 1
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 2
                        beautyConfig.makeupOption.mLipColor = 2
                        enableMakeup(true)
                    }

                    3 -> {  // 第3套美妆 清透
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 3 //眉毛
                        beautyConfig.makeupOption.mBrowColor = 2
                        beautyConfig.makeupOption.mLashType = 5 //眼睫毛
                        beautyConfig.makeupOption.mLashColor = 1
                        beautyConfig.makeupOption.mShadowType = 6 //眼影
                        beautyConfig.makeupOption.mPupilType = 2
                        beautyConfig.makeupOption.mBlushType = 1 //腮红
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 3 //唇彩
                        makeupOption.mLipColor = 3
                        enableMakeup(true)
                    }

                    4 -> {  // 第4套美妆 元气
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 1 //眉毛
                        beautyConfig.makeupOption.mBrowColor = 2
                        beautyConfig.makeupOption.mLashType = 0 //眼睫毛
                        beautyConfig.makeupOption.mLashColor = 0
                        beautyConfig.makeupOption.mShadowType = 0 //眼影
                        beautyConfig.makeupOption.mPupilType = 0
                        beautyConfig.makeupOption.mBlushType = 1 //腮红
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 3 //唇彩
                        beautyConfig.makeupOption.mFacialType = 5
                        makeupOption.mLipColor = 3
                        enableMakeup(true)
                    }

                    5 -> {  // 第5套美妆 淡颜
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 2 //眉毛
                        beautyConfig.makeupOption.mBrowColor = 2
                        beautyConfig.makeupOption.mLashType = 5 //眼睫毛
                        beautyConfig.makeupOption.mLashColor = 2
                        beautyConfig.makeupOption.mShadowType = 0 //眼影
                        beautyConfig.makeupOption.mPupilType = 0
                        beautyConfig.makeupOption.mBlushType = 0 //腮红
                        beautyConfig.makeupOption.mBlushColor = 0
                        beautyConfig.makeupOption.mLipType = 3 //唇彩
                        makeupOption.mLipColor = 3
                        enableMakeup(true)
                    }

                    else -> {
                        beautyConfig.makeupOption.mMakeUpEnable = false
                        enableMakeup(false)
                    }
                }
            }

        // 美妆强度
        var makeupStrength = beautyConfigCache.makeupStrength
            set(value) {
                field = value
                beautyConfigCache.makeupStrength = value
                val makeupObj = JSONObject()
                beautyConfig.makeupOption.mBrowStrength = value
                beautyConfig.makeupOption.mLashStrength = value
                beautyConfig.makeupOption.mShadowStrength = value
                beautyConfig.makeupOption.mPupilStrength = value
                beautyConfig.makeupOption.mBlushStrength = value
                beautyConfig.makeupOption.mLipStrength = value
                try {
                    makeupObj.put("resPath", "$beauty_bundle_dir/makeup")
                    makeupObj.put("enable_mu", beautyConfig.makeupOption.mMakeUpEnable);
                    makeupObj.put("browStrength", beautyConfig.makeupOption.mBrowStrength);
                    makeupObj.put("lashStrength", beautyConfig.makeupOption.mLashStrength);
                    makeupObj.put("shadowStrength", beautyConfig.makeupOption.mShadowStrength);
                    makeupObj.put("pupilStrength", beautyConfig.makeupOption.mPupilStrength);
                    makeupObj.put("blushStrength", beautyConfig.makeupOption.mBlushStrength);
                    makeupObj.put("lipStrength", beautyConfig.makeupOption.mLipStrength);
                    makeupObj.put("browStyle", beautyConfig.makeupOption.mBrowType);
                    makeupObj.put("browColor", beautyConfig.makeupOption.mBrowColor);
                    makeupObj.put("lashStyle", beautyConfig.makeupOption.mLashType);
                    makeupObj.put("lashColor", beautyConfig.makeupOption.mLashColor);
                    makeupObj.put("shadowStyle", beautyConfig.makeupOption.mShadowType);
                    makeupObj.put("pupilStyle", beautyConfig.makeupOption.mPupilType);
                    makeupObj.put("blushStyle", beautyConfig.makeupOption.mBlushType);
                    makeupObj.put("blushColor", beautyConfig.makeupOption.mBlushColor);
                    makeupObj.put("lipStyle", beautyConfig.makeupOption.mLipType);
                    makeupObj.put("lipColor", beautyConfig.makeupOption.mLipColor);
                    rtcEngine?.setExtensionProperty(
                        "agora_video_filters_clear_vision",
                        "clear_vision",
                        "makeup_options",
                        makeupObj.toString(),
                        Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }


        internal fun reset() {
            val beautyConfigCacheInfo = AppCacheManager.beautyConfigCache
            if (!TextUtils.isEmpty(beautyConfigCacheInfo)) {
                beautyConfigCache = GsonUtils.fromJson<BeautyConfigCache>(
                    beautyConfigCacheInfo, BeautyConfigCache::class.java
                )
            }
            beautyConfig = BeautyConfig()
        }

        internal fun resume() {

            basicBeauty = basicBeauty
            filter = filter
            gentlemanFace = gentlemanFace
            ladyFace = ladyFace
            smooth = smooth
            whiten = whiten
            redden = redden
            darkCircles = darkCircles
            nasolabialFolds = nasolabialFolds
            sharpen = sharpen
            filterType = filterType
            faceShape = faceShape
            filterStrength = filterStrength
            makeupType = makeupType
            makeupStrength = makeupStrength

            enlargeEye = enlargeEye
            chinLength = chinLength
            thinFace = thinFace
            shrinkCheekbone = shrinkCheekbone
            longNose = longNose
            narrowNose = narrowNose
            mandible = mandible
            mouthSize = mouthSize
            shrinkJawbone = shrinkJawbone
            hairlineHeight = hairlineHeight

            headScale = headScale
            narrowFace = narrowFace
            adjustContrast = adjustContrast

        }
    }

    fun saveVirtualBackgroundCache(source: String? = "") {
        val virtualBgCache = getVirtualBgCache()
        virtualBgCache.backgroundSourceType = virtualBackgroundSource.backgroundSourceType
        virtualBgCache.modeType = virtualBackgroundSegmentation.modelType
        virtualBgCache.greenCapacity = virtualBackgroundSegmentation.greenCapacity
        virtualBgCache.source = source
        AppCacheManager.beautyBg = GsonUtils.toJson(virtualBgCache)
    }

    fun getVirtualBgCache(): BeautyBgCache {
        val beautyBg = AppCacheManager.beautyBg
        return if (!TextUtils.isEmpty(beautyBg)) {
            GsonUtils.fromJson(beautyBg, BeautyBgCache::class.java)
        } else {
            BeautyBgCache(
                modeType = SegmentationProperty.SEG_MODEL_AI,
                backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
            )
        }
    }

    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
    }
    val virtualBackgroundSegmentation = SegmentationProperty()

    fun resetVirtualBackground(context: Context) {
        val beautyBgCache = getVirtualBgCache()
        virtualBackgroundSegmentation.modelType = beautyBgCache.modeType
        virtualBackgroundSegmentation.greenCapacity = beautyBgCache.greenCapacity
        virtualBackgroundSource.backgroundSourceType = beautyBgCache.backgroundSourceType

        if (!TextUtils.isEmpty(beautyBgCache.source)) {
            virtualBackgroundSource.source = FileUtils.copyFileFromAssets(
                context, beautyBgCache.source!!, context.externalCacheDir!!.absolutePath
            )
        }

        mRtcEngine?.enableVirtualBackground(
            beautyBgCache.backgroundSourceType != VirtualBackgroundSource.BACKGROUND_COLOR,
            virtualBackgroundSource,
            virtualBackgroundSegmentation
        )

    }

    /**
     * 基础美颜扩展功能
     * 亮眼 updateFaceBuffingOption("brighten_eye", value)
     * 去眼袋/黑眼圈 updateFaceBuffingOption("eye_pouch", value)
     * 去法令纹 updateFaceBuffingOption("nasolabial_fold",value)
     * 白牙 updateFaceBuffingOption("whiten_teeth", value)
     */
    @SuppressLint("DefaultLocale")
    internal fun updateFaceBuffingOption(option: String, value: Float) {
        val rtc = rtcEngine ?: return
        val fBuffObj = JSONObject()
        try {
            fBuffObj.put(option, String.format("%.1f", value).toFloat())
            rtc.setExtensionProperty(
                "agora_video_filters_clear_vision",
                "clear_vision",
                "face_buffing_options",
                fBuffObj.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        rtc.setBeautyEffectOptions(true, beautyConfig.beautyOption)
    }
}
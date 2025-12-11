package cn.happy.beautyface.ui.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import cn.happy.beautyface.bean.ModelItem
import cn.happy.beautyface.bean.SenseTimeConfigCache
import cn.happy.beautyface.sensetime.SenseTimeBeautyAPI
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.softsugar.stmobile.STMobileAuthentificationNative
import com.softsugar.stmobile.STMobileEffectNative
import com.softsugar.stmobile.STMobileEffectParams
import com.softsugar.stmobile.STMobileHumanActionNative
import com.softsugar.stmobile.params.STEffectBeautyParams
import com.softsugar.stmobile.params.STEffectBeautyType
import com.softsugar.stmobile.params.STSmoothMode
import kotlin.jvm.java

object SenseTimeBeautySDK {
    private const val TAG = "SenseTimeBeautySDK"

    private val resourcePath = "beauty_sensetime"
    private val humanActionCreateConfig = 0

    private const val MODEL_106 = "models/M_SenseME_Face_Video_Template_p_4.0.0.model" // 106

    private var stickerPackageId = 0

    // 特效句柄
    private var _mobileEffectNative: STMobileEffectNative? = null
    val mobileEffectNative
        get() = _mobileEffectNative ?: throw RuntimeException("Please initMobileEffect firstly!")

    // 人脸识别句柄mSTMobileEffectNative
    private var _humanActionNative: STMobileHumanActionNative? = null
    val humanActionNative
        get() = _humanActionNative ?: throw RuntimeException("Please initBeautySDK firstly!")

    lateinit var beautyConfigCache: SenseTimeConfigCache

    // 美颜配置
    lateinit var beautyConfig: BeautyConfig
    private var beautyAPI: SenseTimeBeautyAPI? = null
    var useLocalBeautyResource = false

    fun saveBeautyConfig() {
        AppCacheManager.beautyConfigCache = GsonUtils.toJson(beautyConfigCache)
    }

    fun getDefaultConfig(): SenseTimeConfigCache {
        return if (TextUtils.isEmpty(AppCacheManager.beautyDefaultConfig)) SenseTimeConfigCache() else GsonUtils.fromJson(
            AppCacheManager.beautyDefaultConfig, SenseTimeConfigCache::class.java
        )
    }

    fun initBeautySDK(context: Context, useLocalBeautyResource: Boolean): Boolean {
        this.useLocalBeautyResource = useLocalBeautyResource
        if (checkLicense(context)) {
            initDefaultBeautyConfig()
            initHumanAction(context)
            initMobileEffect(context)
            return true
        }
        return false
    }

    fun initDefaultBeautyConfig() {
        if (!isInitBeautyConfig()) {
            val beautyConfigCacheInfo = AppCacheManager.beautyConfigCache
            if (!TextUtils.isEmpty(beautyConfigCacheInfo)) {
                beautyConfigCache = GsonUtils.fromJson<SenseTimeConfigCache>(
                    beautyConfigCacheInfo, SenseTimeConfigCache::class.java
                )
            } else {
                beautyConfigCache = getDefaultConfig()
            }
            beautyConfig = BeautyConfig()
        }
    }

    fun isInitBeautyConfig(): Boolean {
        return ::beautyConfig.isInitialized
    }

    fun setDefaultConfig(context: Context) {
        beautyConfigCache = getDefaultConfig()
        beautyConfig = BeautyConfig()
        beautyConfig.resume()
    }

    fun clearShape() {
        beautyConfigCache.faceShapeEnable = false
        beautyConfig.shrinkFace = 0.0f
        beautyConfig.enlargeEye = 0.0f
        beautyConfig.smallFace = 0.0f
        beautyConfig.narrowFace = 0.0f
        beautyConfig.circleEye = 0.0f
        beautyConfig.roundThinFace = 0.0f
        beautyConfig.longThinFace = 0.0f
        beautyConfig.ladyThinFace = 0.0f
        beautyConfig.naturalThinFace = 0.0f
    }

    fun clearLittleShape() {
        beautyConfigCache.littleFaceShapeEnable = false
        beautyConfig.smallHead = 0.0f
        beautyConfig.thinFace = 0.0f
        beautyConfig.vChin = 0.0f
        beautyConfig.vFace = 0.0f
        beautyConfig.chinLength = 0.0f
        beautyConfig.shrinkJawbone = 0.0f
        beautyConfig.shrinkCheekbone = 0.0f
        beautyConfig.narrowNose = 0.0f
        beautyConfig.noseTip = 0.0f
        beautyConfig.noseLength = 0.0f
        beautyConfig.openCanthus = 0.0f
        beautyConfig.openExternalCanthus = 0.0f
        beautyConfig.brightEye = 0.0f
        beautyConfig.nasolabialFolds = 0.0f
        beautyConfig.darkCircles = 0.0f

        beautyConfig.enlargeEye = 0.0f
        beautyConfig.hairlineHeight = 0.0f
        beautyConfig.mouthSize = 0.0f
        beautyConfig.whiteTeeth = 0.0f
    }


    fun clearBeauty() {
        beautyConfigCache.beautyEnable = false
        beautyConfig.whiten = 0.0f
        beautyConfig.redden = 0.0f
        beautyConfig.smooth = 0.0f
        beautyConfig.skinSmooth = 0.0f
    }

    fun clearGroupAdjust() {
        beautyConfig.sharpen = 0.0f
        beautyConfig.clear = 0.0f
        beautyConfig.saturation = 0.0f
        beautyConfig.contrast = 0.0f
    }

    fun isClearGroupAdjust(): Boolean {
        return beautyConfig.sharpen == 0.0f && beautyConfig.clear == 0.0f && beautyConfig.saturation == 0.0f && beautyConfig.contrast == 0.0f
    }

    fun unInitBeautySDK() {
        beautyAPI = null
        unInitMobileEffect()
        unInitHumanActionNative()
        beautyConfig.reset()
    }

    private fun initMobileEffect(context: Context) {
        if (_mobileEffectNative != null) {
            return
        }
        _mobileEffectNative = STMobileEffectNative()
        val result =
            _mobileEffectNative?.createInstance(context, STMobileEffectNative.EFFECT_CONFIG_NONE)
        _mobileEffectNative?.setParam(STMobileEffectParams.EFFECT_PARAM_QUATERNION_SMOOTH_FRAME, 5f)
        Log.d(TAG, "SenseTime >> STMobileEffectNative create result : $result")
    }

    private fun unInitMobileEffect() {
        _mobileEffectNative?.destroyInstance()
        _mobileEffectNative = null
    }

    fun getAssetsPath(context: Context): String {
        return context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourcePath"
    }

    private fun checkLicense(context: Context): Boolean {
//        val licData = getLicenseData()
//        return STLicenseUtils.checkLicenseFromBuffer(context,licData,false)
//
        val license = if (useLocalBeautyResource) ReadAssetsJsonFileUtils.getAssetsString(
            context, "$resourcePath/license/SenseME.lic"
        ) else "${getAssetsPath(context)}/license/SenseME.lic"
        if (TextUtils.isEmpty(license)) {
            return false
        }

        val activeCode =
            if (useLocalBeautyResource) STMobileAuthentificationNative.generateActiveCodeFromBuffer(
                context,
                license,
                license.length
            ) else STMobileAuthentificationNative.generateActiveCode(context, license)
        Log.d(TAG, "SenseTime >> checkLicense successfully! activeCode=$activeCode")
        return true
    }

    private fun initHumanAction(context: Context) {
        if (_humanActionNative != null) {
            return
        }
        _humanActionNative = STMobileHumanActionNative()
        val result = if (useLocalBeautyResource) _humanActionNative?.createInstanceFromAssetFile(
            "$resourcePath/$MODEL_106",
            humanActionCreateConfig,
            context.assets
        )
        else _humanActionNative?.createInstance(
            "${getAssetsPath(context)}/$MODEL_106",
            humanActionCreateConfig
        )
        Log.d(TAG, "SenseTime >> STMobileHumanActionNative create result : $result")

        if (result != 0) {
            return
        }

        ThreadUtils.executeByIo<Boolean>(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean? {
                val json: String? = ReadAssetsJsonFileUtils.readJsonFromFile(
                    "${getAssetsPath(context)}/json_data/json_models.json"
                )
                if (!TextUtils.isEmpty(json)) {
                    val type = GsonUtils.getListType(ModelItem::class.java)
                    val modelItemList: MutableList<ModelItem?> =
                        GsonUtils.fromJson<MutableList<ModelItem?>?>(json, type)
                    for (i in modelItemList.indices) {
                        val root: String =
                            getAssetsPath(context)
                        val sdPath = root + "/" + modelItemList[i]?.model_asset_path
                        _humanActionNative?.addSubModel(
                            sdPath
                        )
                    }
                }
                return true
            }

            override fun onSuccess(result: Boolean?) {
            }

        })


        // 其他模型配置

        // 背景分割羽化程度[0,1](默认值0.35),0 完全不羽化,1羽化程度最高,在strenth较小时,羽化程度基本不变.值越大,前景与背景之间的过度边缘部分越宽.
        // _humanActionNative?.setParam(
        //     STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_BACKGROUND_BLUR_STRENGTH,
        //     0.35f
        // )
        // 设置face mesh结果输出坐标系,(0: 屏幕坐标系， 1：3d世界坐标系， 2:3d摄像机坐标系,是摄像头透视投影坐标系, 原点在摄像机 默认是0）
        // _humanActionNative?.setParam(
        //     STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_FACE_MESH_OUTPUT_FORMAT,
        //     1.0f
        // )
        // 设置mesh渲染模式
        // _humanActionNative?.setParam(
        //     STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_MESH_MODE,
        //     STCommonNative.MESH_CONFIG.toFloat()
        // )
        // 设置人头实例分割
        // _humanActionNative?.setParam(
        //     STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_INSTANCE,
        //     1.0f
        // )
    }


    private fun unInitHumanActionNative() {
        _humanActionNative?.destroyInstance()
        _humanActionNative = null
    }


    internal fun setBeautyAPI(beautyAPI: SenseTimeBeautyAPI) {
        this.beautyAPI = beautyAPI
        beautyConfig.resume()
    }

    private fun runOnBeautyThread(run: () -> Unit) {
        beautyAPI?.runOnProcessThread(run) ?: run.invoke()
    }

    open class BeautyConfig {
        /*美颜-------------start */
        // 美白
        var whiten = beautyConfigCache.whiten
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    if (AppCacheManager.openWhitenSkinMask){
                        effectNative.setBeautyParam(STEffectBeautyParams.ENABLE_WHITEN_SKIN_MASK, 1f)
                    }
                    beautyConfigCache.whiten = value
                    beautyConfigCache.beautyEnable = true
                    effectNative.setBeautyMode(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN,
                        STEffectBeautyType.WHITENING1_MODE
                    )
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN,
                        value
                    )
                }
            }

        // 红润
        var redden = beautyConfigCache.redden
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.beautyEnable = true
                    beautyConfigCache.redden = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN,
                        value
                    )
                }
            }

        // 磨皮
        var smooth = beautyConfigCache.smooth
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.beautyEnable = true
                    beautyConfigCache.smooth = value
                    effectNative.setBeautyMode(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
                        STSmoothMode.EFFECT_SMOOTH_FACE_EVEN
                    )
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
                        value
                    )
                }
            }

        //身体磨皮
        var skinSmooth = beautyConfigCache.skinSmooth
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.beautyEnable = true
                    beautyConfigCache.skinSmooth = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_BASE_SKIN_SMOOTH,
                        value
                    )
                }
            }
        /*美颜-------------end */


        /*美型-------------start */

        // 瘦脸
        var shrinkFace = beautyConfigCache.shrinkFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.shrinkFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE,
                        value
                    )
                }
            }

        // 大眼
        var enlargeEye = beautyConfigCache.enlargeEye
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.enlargeEye = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE,
                        value
                    )
                }
            }

        //小脸
        var smallFace = beautyConfigCache.smallFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.smallFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_JAW,
                        value
                    )
                }
            }

        //窄脸
        var narrowFace = beautyConfigCache.narrowFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.narrowFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_NARROW_FACE,
                        value
                    )
                }
            }

        // 圆眼
        var circleEye = beautyConfigCache.circleEye
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.circleEye = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ROUND_EYE,
                        value
                    )
                }
            }


        // 圆脸瘦脸
        var roundThinFace = beautyConfigCache.roundThinFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.roundThinFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_ROUND_FACE,
                        value
                    )
                }
            }

        // 长脸瘦脸
        var longThinFace = beautyConfigCache.longThinFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.longThinFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_LONG_FACE,
                        value
                    )
                }
            }

        // 女神瘦脸
        var ladyThinFace = beautyConfigCache.ladyThinFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.ladyThinFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_GODDESS_FACE,
                        value
                    )
                }
            }

        // 自然瘦脸
        var naturalThinFace = beautyConfigCache.naturalThinFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.faceShapeEnable = true
                    beautyConfigCache.naturalThinFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_NATURAL_FACE,
                        value
                    )
                }
            }
        /*美型-------------end */


        /*微整形-------------start */

        //小头
        var smallHead = beautyConfigCache.smallHead
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.smallHead = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THINNER_HEAD,
                        value
                    )
                }
            }

        //瘦脸型
        var thinFace = beautyConfigCache.thinFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.thinFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE,
                        value
                    )
                }
            }

        //v下巴
        var vChin = beautyConfigCache.vChin
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.vChin = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_FACE_V_SHAPE,
                        value
                    )
                }
            }

        //v脸
        var vFace = beautyConfigCache.vFace
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.vFace = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_FACE_V_SHAPE,
                        value
                    )
                }
            }

        // 下巴
        var chinLength = beautyConfigCache.chinLength
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.chinLength = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, value
                    )
                }
            }

        // 下颌骨
        var shrinkJawbone = beautyConfigCache.shrinkJawbone
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.shrinkJawbone = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE,
                        value
                    )
                }
            }

        // 瘦颧骨
        var shrinkCheekbone = beautyConfigCache.shrinkCheekbone
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.shrinkCheekbone = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE,
                        value
                    )
                }
            }

        // 瘦鼻
        var narrowNose = beautyConfigCache.narrowNose
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.narrowNose = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE,
                        value
                    )
                }
            }

        // 瘦鼻头
        var noseTip = beautyConfigCache.noseTip
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.noseTip = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NOSE_TIP,
                        value
                    )
                }
            }

        // 长鼻
        var noseLength = beautyConfigCache.noseLength
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.noseLength = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NOSE_LENGTH,
                        value
                    )
                }
            }

        // 开眼角
        var openCanthus = beautyConfigCache.openCanthus
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.openCanthus = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_OPEN_CANTHUS,
                        value
                    )
                }
            }

        // 开外眼角
        var openExternalCanthus = beautyConfigCache.openExternalCanthus
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.openExternalCanthus = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_OPEN_EXTERNAL_CANTHUS,
                        value
                    )
                }
            }

        // 亮眼
        var brightEye = beautyConfigCache.enlargeEye
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.brightEye = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE,
                        value
                    )
                }
            }


        // 祛黑眼圈
        var darkCircles = beautyConfigCache.darkCircles
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.darkCircles = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES,
                        value
                    )
                }
            }

        // 祛法令纹
        var nasolabialFolds = beautyConfigCache.nasolabialFolds
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.nasolabialFolds = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS,
                        value
                    )
                }
            }


        // 美牙
        var whiteTeeth = beautyConfigCache.whiteTeeth
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.whiteTeeth = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH,
                        value
                    )
                }
            }

        // 额头
        var hairlineHeight = beautyConfigCache.hairlineHeight
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.littleFaceShapeEnable = true
                    beautyConfigCache.hairlineHeight = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT,
                        value
                    )
                }
            }


        // 嘴形
        var mouthSize = beautyConfigCache.mouthSize
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.mouthSize = value
                    beautyConfigCache.littleFaceShapeEnable = true
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE,
                        value
                    )
                }
            }

        /*微整形-------------end */


        /*滤镜-------------start */
        var filterMode: String? = beautyConfigCache.filterModel
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.filterModel = value
                    effectNative.setBeauty(STEffectBeautyType.EFFECT_BEAUTY_FILTER, value)
                }
            }
        var filterLength: Float = beautyConfigCache.filterLength
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.filterLength = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_FILTER,
                        value
                    )
                }
            }
        /*滤镜-------------end */


        /*调整-------------start */
        // 饱和度
        var saturation = beautyConfigCache.saturation
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.saturation = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION,
                        value
                    )
                }
            }

        // 对比度
        var contrast = beautyConfigCache.adjustContrast
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.adjustContrast = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST,
                        value
                    )
                }
            }

        // 锐化
        var sharpen = beautyConfigCache.sharpen
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.sharpen = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN,
                        value
                    )
                }
            }


        // 清晰度
        var clear = beautyConfigCache.clear
            set(value) {
                field = value
                val effectNative = _mobileEffectNative ?: return
                runOnBeautyThread {
                    beautyConfigCache.clear = value
                    effectNative.setBeautyStrength(
                        STEffectBeautyType.EFFECT_BEAUTY_TONE_CLEAR,
                        value
                    )
                }
            }
        /*调整-------------end */


        // 美妆
        var makeUp: MakeUpItem? = beautyConfigCache.makeUp
            set(value) {
                val shouldReset = field?.path != value?.path
                field = value
                beautyConfigCache.makeUp = value
                runOnBeautyThread {
                    if (value == null) {
                        _mobileEffectNative?.setBeauty(
                            STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                            null
                        )
                    } else {
                        if (shouldReset) {
                            if (useLocalBeautyResource) {
                                _mobileEffectNative?.setBeautyFromAssetsFile(
                                    STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                                    "$resourcePath/${value.path}",
                                    value.context.assets
                                )
                            } else {
                                val path =
                                    value.context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourcePath"
                                _mobileEffectNative?.setBeauty(
                                    STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                                    "$path/${value.path}",
                                )
                            }
                        }
                        _mobileEffectNative?.setBeautyStrength(
                            STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                            value.strength
                        )
                    }
                }
            }

        // 贴纸
        var sticker: StickerItem? = null
            set(value) {
                field = value
                runOnBeautyThread {
                    if (value == null) {
                        if (stickerPackageId > 0) {
                            _mobileEffectNative?.removeEffect(stickerPackageId)
                            stickerPackageId = 0
                        }
                    } else {
                        if (useLocalBeautyResource) {
                            stickerPackageId = _mobileEffectNative?.changePackageFromAssetsFile(
                                "$resourcePath/${value.path}",
                                value.context.assets
                            ) ?: 0
                        } else {
                            val path =
                                value.context.getExternalFilesDir(null)?.absolutePath + "/assets/sticker_face_shape/${value.path}"
                            stickerPackageId = _mobileEffectNative?.changePackage(
                                path
                            ) ?: 0
                        }
                    }
                }
            }

        internal fun reset() {
            val beautyConfigCacheInfo = AppCacheManager.beautyConfigCache
            if (!TextUtils.isEmpty(beautyConfigCacheInfo)) {
                beautyConfigCache = GsonUtils.fromJson<SenseTimeConfigCache>(
                    beautyConfigCacheInfo, SenseTimeConfigCache::class.java
                )
            } else {
                beautyConfigCache = getDefaultConfig()
            }
            beautyConfig = BeautyConfig()
            makeUp = null
            sticker = null
        }

        internal fun resume() {
            /*美颜-------------start */
            whiten = whiten
            redden = redden
            smooth = smooth
            skinSmooth = skinSmooth
            /*美颜-------------end */

            /*美型-------------start */
            shrinkFace = shrinkFace
            enlargeEye = enlargeEye
            smallFace = smallFace
            narrowFace = narrowFace
            circleEye = circleEye
            roundThinFace = roundThinFace
            longThinFace = longThinFace
            ladyThinFace = ladyThinFace
            naturalThinFace = naturalThinFace
            /*美型-------------end */

            /*微美型-------------start */
            smallHead = smallHead
            thinFace = thinFace
            vChin = vChin
            vFace = vFace
            chinLength = chinLength
            shrinkCheekbone = shrinkCheekbone
            shrinkJawbone = shrinkJawbone
            narrowNose = narrowNose
            noseTip = noseTip
            noseLength = noseLength
            openCanthus = openCanthus
            openExternalCanthus = openExternalCanthus
            brightEye = brightEye
            darkCircles = darkCircles
            nasolabialFolds = nasolabialFolds
            whiteTeeth = whiteTeeth
            hairlineHeight = hairlineHeight
            mouthSize = mouthSize
            /*微美型-------------end */

            /*滤镜-------------start */
            filterMode = filterMode
            filterLength = filterLength
            /*滤镜-------------end */

            /*调整-------------start */
            clear = clear
            sharpen = sharpen
            saturation = saturation
            contrast = contrast
            /*调整-------------end */




            makeUp = makeUp
            sticker = sticker
        }
    }

    data class MakeUpItem(
        val context: Context,
        val path: String,
        val strength: Float
    )

    data class StickerItem(
        val context: Context,
        val path: String
    )
}
package cn.happy.beautyface.bean

import cn.yanhu.commonres.manager.AppCacheManager


/**
 * @author: zhengjun
 * created: 2025/6/9
 * desc:
 */
data class BeautyConfigCache(
    var basicBeauty: Boolean = true,
    var filter: Boolean = true,
    var adjustContrast: Float = 0.5f, //对比度
    var smooth: Float = 0.8f,//磨皮
    var whiten: Float = 0.6f,//美白
    var redden: Float = 0.5f,//红润
    var darkCircles: Float = 0.7f, //去黑眼圈
    var nasolabialFolds: Float = 0.7f,//去法令纹
    var filterType: String = "QISE",
    var filterStrength: Float = 0.5f,
    var sharpen: Float = 0.3f,
    var faceShape: Boolean = true,
    var enlargeEye: Int = 60,
    var chinLength: Int = -30,
    var thinFace: Int = 70,//瘦脸
    var shrinkCheekbone: Int = 50,//瘦颧骨
    var longNose: Int = 0,//长鼻
    var narrowNose: Int = 10,
    var mouthSize: Int = 0,
    var shrinkJawbone: Int = 40,
    var hairlineHeight: Int = 0,
    var gentlemanFace: Int = 0,
    var ladyFace: Int = 40,
    var makeupType: Int = if (AppCacheManager.isWoman()) 4 else 0,
    var makeupStrength: Float = 0.5f,
    var headScale: Int = 30,//小头
    var narrowFace:Int =30//窄脸
)
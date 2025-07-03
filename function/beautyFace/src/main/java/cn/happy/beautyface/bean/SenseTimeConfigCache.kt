package cn.happy.beautyface.bean

import cn.happy.beautyface.ui.utils.SenseTimeBeautySDK.MakeUpItem


/**
 * @author: zhengjun
 * created: 2025/6/9
 * desc:
 */
data class SenseTimeConfigCache(
    var beautyEnable: Boolean = true,
    var faceShapeEnable: Boolean = true,
    var littleFaceShapeEnable: Boolean = true,

    var adjustContrast: Float = 0.0f, //对比度
    var smooth: Float = 0.0f,//磨皮
    var skinSmooth: Float = 0.0f,//身体磨皮
    var whiten: Float = 0.0f,//美白
    var redden: Float = 0.0f,//红润
    var darkCircles: Float = 0.0f, //去黑眼圈
    var nasolabialFolds: Float = 0.0f,//去法令纹
    var saturation: Float = 0.0f,
    var sharpen: Float = 0.0f,//锐化
    var clear: Float = 0.0f,//清晰
    var circleEye: Float = 0.0f, //圆眼
    var enlargeEye: Float = 0.0f, //大眼
    var brightEye: Float = 0.0f, //亮眼
    var chinLength: Float = 0f, //下巴
    var roundThinFace: Float = 0.0f,//圆瘦脸
    var longThinFace: Float = 0.0f,//长瘦脸
    var ladyThinFace: Float = 0.0f,//女神瘦脸
    var naturalThinFace: Float = 0.0f,//自然瘦脸
    var shrinkFace: Float = 0.0f,//瘦脸
    var thinFace: Float = 0.0f,//瘦脸型
    var smallFace: Float = 0.0f,//小脸
    var narrowFace: Float = 0.0f,//窄脸
    var shrinkCheekbone: Float = 0.0f,//瘦颧骨
    var vChin: Float = 0.0f,//v下巴
    var vFace: Float = 0.0f, //v脸
    var noseTip: Float = 0.0f,//瘦鼻头
    var noseLength: Float =0.0f,//长鼻
    var openCanthus: Float =0.0f,//开眼角
    var openExternalCanthus: Float =0.0f,//开外眼角
    var narrowNose: Float = 0.0f, //瘦鼻
    var mouthSize: Float = 0f,
    var shrinkJawbone: Float = 0.0f,
    var whiteTeeth: Float = 0.0f,
    var hairlineHeight: Float = 0f,
    var makeUp: MakeUpItem? = null,
    var filterModel: String?=null,
    var filterLength: Float = 0.0f,
    var smallHead: Float = 0.0f
)
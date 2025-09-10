package cn.yanhu.commonres.bean

import androidx.core.graphics.toColorInt

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
data class WheelViewPopInfo(
    val leftName: String,
    val rightName: String = "确定",
    val topBgColor: Int = "#161727".toColorInt(),
    val bgColor: Int = "#161727".toColorInt(),
    val leftBtnColor: Int = "#ffffff".toColorInt(),
    val rightBtnColor: Int= "#F8459B".toColorInt(),
    val itemNormalColor: Int = "#66FFFFFF".toColorInt(),
    val itemSelectedColor: Int= "#FFFFFF".toColorInt(),
    val dividerColor:Int = "#1AFFFFFF".toColorInt()
)
package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2026/1/13
 * desc:
 */
data class BubbleInfo(val type: String, val content: BubbleContent) : Serializable {
    data class BubbleContent(
        val mode: String,
        val angle: Int,
        val gradientList: MutableList<GradientBean>
    ) : Serializable

    data class GradientBean(val color: String) : Serializable
}
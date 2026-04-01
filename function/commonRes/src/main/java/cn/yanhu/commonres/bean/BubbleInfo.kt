package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2026/1/13
 * desc:
 */
data class BubbleInfo(val type: String, val content: BubbleContent) : Serializable {
    companion object{
        const val TYPE_NINEPATCH = "ninePatch"
    }
    data class BubbleContent(
        val mode: String,
        val angle: Int,
        val image: String,
        val original: String,
        val gradientList: MutableList<GradientBean>
    ) : Serializable

    data class GradientBean(val color: String) : Serializable
}
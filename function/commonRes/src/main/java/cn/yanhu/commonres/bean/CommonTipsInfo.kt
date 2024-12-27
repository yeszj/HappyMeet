package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2023/11/21
 * desc:
 */
data class CommonTipsInfo(
    val title: String,
    val desc: CharSequence,
    val btn: String,
    val isAutoDismiss: Boolean = true,
    var drawableId:Int = 0,
    var showClose:Boolean = true
)
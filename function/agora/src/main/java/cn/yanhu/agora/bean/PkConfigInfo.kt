package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
data class PkConfigInfo(
    val pkTypeList: MutableList<Int>,
    var pkTypeDefaultIndex: Int,
    val pkTimeList: MutableList<PlTimeInfo>,
    var pkTimeDefaultIndex: Int
){
    data class PlTimeInfo(val showText: String,val choiceType: Int)
}
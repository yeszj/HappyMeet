package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
data class LiveIncomeDetailInfo(
    val typeDesc: String,
    val time: String,
    val roseNum: String,
    val list: MutableList<IncomeInfo>
) {
    data class IncomeInfo(
        val userId: String,
        val portrait: String,
        val nickName: String,
        val identity: Int,
        val roseNum: String,
        val tcRoseNum: String
    ){
        fun isOwner():Boolean{
            return identity == 1
        }
    }
}
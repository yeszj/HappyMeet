package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class WalletRecordInfo(
    val title: String,
    val userInfo: BaseUserInfo?,
    val time: String,
    val num: String,
    val desc: String,
    val list: MutableList<WalletRecordInfo>
){
    fun isIncome():Boolean{
        return num.startsWith("+")
    }
}
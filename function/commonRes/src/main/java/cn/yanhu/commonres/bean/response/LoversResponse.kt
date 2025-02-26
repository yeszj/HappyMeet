package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.GiftInfo

/**
 * @author: zhengjun
 * created: 2025/2/18
 * desc:
 */
data class LoversResponse(
    val viewInfo: BaseUserInfo,
    var loverInfo: BaseUserInfo?,
    val remainDay:String,
    val loversType: Int?,
    val ruleImg: String,
    val giftList: MutableList<GiftInfo>,
    val cancalPrice:String
)
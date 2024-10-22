package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.GiftInfo
import java.math.BigDecimal

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
data class GiftResponse(var roseNum: BigDecimal, val list: MutableList<GiftInfo>)
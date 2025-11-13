package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.bean.RoseRechargeBean

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
data class RoseRechargeResponse(
    val roseBalance: String,
    val isFirstRecharge: Boolean,
    val rechargeAgreement: String,
    val defaultSelect: Int,
    val list: MutableList<RoseRechargeBean>,
    val bannerBean: BannerBean?,
    val rewardInfo: RewardInfo?,
    val scrollText: String?
) {
    data class RewardInfo(val rewardIcon: String, val rewardDesc: String, var title:String, var btn:String)
}
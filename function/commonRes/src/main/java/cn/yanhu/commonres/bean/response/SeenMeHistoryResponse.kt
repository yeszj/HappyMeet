package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.SeenMeHistoryInfo

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class SeenMeHistoryResponse {
    val historyViewPages: MutableList<SeenMeHistoryInfo> = mutableListOf()
    var isHasPermission = false
}
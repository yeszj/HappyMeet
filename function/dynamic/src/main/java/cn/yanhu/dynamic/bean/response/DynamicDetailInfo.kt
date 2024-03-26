package cn.yanhu.dynamic.bean.response

import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.bean.DynamicInfo

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
class DynamicDetailInfo {
    var trendsInfo:DynamicInfo?=null
    var trendsComments:MutableList<DiscussInfo> = mutableListOf()
}
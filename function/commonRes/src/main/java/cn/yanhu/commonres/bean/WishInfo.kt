package cn.yanhu.commonres.bean


/**
 * @author: zhengjun
 * created: 2025/3/27
 * desc:
 */
class WishInfo(
    var posId:Int,
    var sunNum: Int,
    var finishNum: Int,
    var portraitList:MutableList<String>
) : GiftInfo()
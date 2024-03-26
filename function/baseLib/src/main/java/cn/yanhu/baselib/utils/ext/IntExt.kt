package cn.yanhu.baselib.utils.ext

import com.blankj.utilcode.util.Utils


/**
 * 获取stringId对应的字符串
 * @receiver Int
 */
fun Int.resIdToString(){
    Utils.getApp().getString(this)
}
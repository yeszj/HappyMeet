package cn.yanhu.dynamic

import cn.yanhu.dynamic.bean.DiscussInfo


/**
 * @author: zhengjun
 * created: 2023/7/3
 * desc:
 */
interface OnClickDiscussListener {
    fun onClickItem(position:Int,item: DiscussInfo)
}
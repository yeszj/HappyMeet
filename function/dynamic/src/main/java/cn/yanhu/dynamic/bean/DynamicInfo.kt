package cn.yanhu.dynamic.bean

import android.text.TextUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.AppCacheManager

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
class DynamicInfo : BaseUserInfo() {
    val images: MutableList<String> = mutableListOf()
    val info: String = ""
    val time: String = ""
    val isAuth = false
    var trendsLikeCount = 0
    var trendsCommentCount = 0
    var trendsIsLike: Boolean = false
    val dynamicCity: String? = ""
    var sectionInfRes: MutableList<BaseUserInfo> = mutableListOf()
    var roomId:String? = ""
    fun getTimeDesc():CharSequence{
        return if (TextUtils.isEmpty(province)){
            time
        }else{
            Spans.builder()
                .text(time)
                .text(" · $province")
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
                .build()
        }
    }

    fun isSelf():Boolean{
        return userId==AppCacheManager.userId
    }
}
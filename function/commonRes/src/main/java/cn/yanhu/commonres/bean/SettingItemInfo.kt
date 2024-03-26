package cn.yanhu.commonres.bean

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
data class SettingItemInfo(
    val bgDrawable: Drawable? = ContextCompat.getDrawable(
        ActivityUtils.getTopActivity(),
        cn.yanhu.baselib.R.color.white
    ),
    val name: String,
    var desc: String? = "",
    var isShowArrow: Boolean = true,
    val showDivider: Boolean = false,
    var pageUrl:String = ""
)
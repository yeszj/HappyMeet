package cn.yanhu.commonres.manager

import android.widget.TextView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2023/3/22
 * desc:
 */
object SpansManager {
    fun getSpan(text: String, textSize: Int, colorId: Int): Spans {
        return try {
            Spans.builder().text(text).size(CommonUtils.getSpByDimen(textSize))
                .color(colorId).build()
        } catch (e: Exception) {
            Spans.builder().text(text).size(textSize).color(colorId).build()
        }
    }

    fun getSpan(text: String, colorId: Int): Spans {
        return Spans.builder().text(text).color(colorId).build()
    }

    fun getClickSpan(tv: TextView, text: String, textSize: Int, colorId: Int,onAllSpanClickListener: CustomClickSpan.OnAllSpanClickListener): Spans {
        return try {
            Spans.builder().text(text).size(CommonUtils.getSpByDimen(textSize))
                .color(CommonUtils.getColor(colorId)).click(tv,CustomClickSpan(ActivityUtils.getTopActivity(),colorId,onAllSpanClickListener)).build()
        } catch (e: Exception) {
            Spans.builder().text(text).size(textSize).color(CommonUtils.getColor(colorId)).build()
        }
    }
}
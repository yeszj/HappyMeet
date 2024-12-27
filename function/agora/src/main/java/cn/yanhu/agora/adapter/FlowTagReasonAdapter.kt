package cn.yanhu.agora.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import cn.yanhu.agora.R
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter

/**
 * @author: zhengjun
 * created: 2023/12/6
 * desc:
 */
class FlowTagReasonAdapter(val context: Context, list: MutableList<String>): TagAdapter<String>(list) {
    override fun getView(p0: FlowLayout?, p1: Int, p2: String): View {
        val tv = View.inflate(context, R.layout.adapter_reason_tag_item, null) as TextView
        tv.text = p2
        return tv
    }

    override fun setSelected(p0: Int, p1: String?): Boolean {
        return false
    }
}
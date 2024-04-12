package cn.yanhu.commonres.loading

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.adapter.LoadingAdapter
import com.kingja.loadsir.callback.Callback

/**
 * @author: zhengjun
 * created: 2023/6/5
 * desc:
 */
class CommonLoadingCallBack(private val layoutId:Int) : Callback() {

    override fun onCreateView(): Int {
        return R.layout.loading_blinddate_page
    }
    override fun onViewCreate(context: Context, view: View) {
        super.onViewCreate(context, view)
        val rvRoom = view.findViewById<RecyclerView>(R.id.rv_room)
        rvRoom.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        rvRoom.adapter = LoadingAdapter(context, layoutId)
    }

    override fun onReloadEvent(context: Context, view: View): Boolean {
        return true
    }
}
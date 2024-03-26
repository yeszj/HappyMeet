package cn.yanhu.commonres.loading

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.adapter.LoadingAdapter
import com.kingja.loadsir.callback.Callback

/**
 * @author: zhengjun
 * created: 2023/6/5
 * desc:
 */
class MainLoadingCallBack : Callback() {

    override fun onCreateView(): Int {
        return R.layout.loading_main_page
    }
    override fun onViewCreate(context: Context, view: View) {
        super.onViewCreate(context, view)
        val rvTab = view.findViewById<RecyclerView>(R.id.rv_tab)
        rvTab.adapter = LoadingAdapter(context, R.layout.adapter_loading_tab_item,2)

        val rvRoom = view.findViewById<RecyclerView>(R.id.rv_room)
        rvRoom.adapter = LoadingAdapter(context, R.layout.adapter_loading_room_item)
    }

    override fun onDetach() {
        super.onDetach()
    }
    override fun onReloadEvent(context: Context, view: View): Boolean {
        return true
    }
}
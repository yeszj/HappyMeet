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
class RoomLoadingCallBack : Callback() {

    override fun onCreateView(): Int {
        return R.layout.loading_blinddate_page
    }
    override fun onViewCreate(context: Context, view: View) {
        super.onViewCreate(context, view)
        val rvRoom = view.findViewById<RecyclerView>(R.id.rv_room)
        rvRoom.adapter = LoadingAdapter(context, R.layout.adapter_loading_room_item)
    }

    override fun onReloadEvent(context: Context, view: View): Boolean {
        return true
    }
}
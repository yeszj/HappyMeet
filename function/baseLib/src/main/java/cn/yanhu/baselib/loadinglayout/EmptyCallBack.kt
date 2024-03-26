package cn.yanhu.baselib.loadinglayout

import android.content.Context
import android.view.View
import cn.yanhu.baselib.R
import com.kingja.loadsir.callback.Callback

class EmptyCallBack : Callback() {

    override fun onCreateView(): Int  = R.layout.layout_empty

    override fun onViewCreate(context: Context?, view: View?) {
        view?.isClickable = false
    }
    override fun onReloadEvent(context: Context?, view: View?): Boolean {
        return true
    }
}
package cn.yanhu.baselib.loadinglayout

import android.content.Context
import android.view.View
import cn.yanhu.baselib.R
import com.kingja.loadsir.callback.Callback

class LoadingCallBack : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_loadings
    }

    override fun onReloadEvent(context: Context?, view: View?): Boolean {
        return true
    }

    override fun onDetach() {
        super.onDetach()
    }
}
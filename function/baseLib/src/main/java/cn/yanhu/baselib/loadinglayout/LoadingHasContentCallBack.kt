package cn.yanhu.baselib.loadinglayout

import android.content.Context
import android.view.View
import cn.yanhu.baselib.R
import com.kingja.loadsir.callback.Callback

class LoadingHasContentCallBack : Callback() {
    override fun onCreateView(): Int {
        return R.layout.layout_text_loading
    }

    override fun getSuccessVisible(): Boolean {
        return true
    }

    override fun onReloadEvent(context: Context?, view: View?): Boolean {
        return true
    }

    override fun onDetach() {
        super.onDetach()
    }

}
package cn.yanhu.baselib.loadinglayout


import android.content.Context
import android.view.View
import cn.yanhu.baselib.R
import com.kingja.loadsir.callback.Callback


/**
 * 错误页面回调
 *
 * @author woochen
 * @time 2018/9/12 10:59
 */

class ErrorCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_errors
    }
    override fun onViewCreate(context: Context?, view: View?) {

    }
}

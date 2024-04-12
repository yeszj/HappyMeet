package cn.yanhu.baselib.loadinglayout

import com.blankj.utilcode.util.ThreadUtils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

/**
 * @author: zhengjun
 * created: 2024/3/29
 * desc:
 */
object LoadingManager {
    fun initCustomLoading(loadCallBack: Callback): LoadSir{
        return LoadSir.Builder()
            .addCallback(ErrorCallback())
            .addCallback(EmptyCallBack())
            .addCallback(LoadingHasContentCallBack())
            .setDefaultCallback(SuccessCallback::class.java)
            .addCallback(loadCallBack)
            .build()
    }

    fun showLoading(loadService: LoadService<*>, callback: Class<out Callback?>?){
        ThreadUtils.getMainHandler().postDelayed({
            loadService.showCallback(callback)
        },50)
    }

    fun showContent(loadService: LoadService<*>){
        loadService.showSuccess()
    }

    fun showError(loadService: LoadService<*>){
        loadService.showCallback(ErrorCallback::class.java)
    }

    fun showEmpty(loadService: LoadService<*>){
        loadService.showCallback(EmptyCallBack::class.java)
    }
}
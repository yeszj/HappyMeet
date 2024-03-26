package cn.yanhu.baselib.config

import android.content.Context
import androidx.startup.Initializer
import cn.yanhu.baselib.cache.ConfigPref
import cn.yanhu.baselib.cache.UserPref
import cn.yanhu.baselib.loadinglayout.EmptyCallBack
import cn.yanhu.baselib.loadinglayout.ErrorCallback
import cn.yanhu.baselib.loadinglayout.LoadingCallBack
import cn.yanhu.baselib.loadinglayout.LoadingHasContentCallBack
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadSir

class LibInitializer : Initializer<Unit> {
    override fun create(mContenx: Context) {
        //loadSir
        LoadSir.beginBuilder().addCallback(ErrorCallback())
            .addCallback(EmptyCallBack())
            .addCallback(LoadingCallBack())
            .addCallback(LoadingHasContentCallBack())
            .setDefaultCallback(SuccessCallback::class.java).commit()
        UserPref.init(mContenx)
        ConfigPref.init(mContenx)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}
package cn.yanhu.commonres.manager

import android.content.Context
import com.danikula.videocache.HttpProxyCacheServer

/**
 * @author: zhengjun
 * created: 2024/1/3
 * desc:
 */
object VideoCacheProxy {
    private fun newProxy(context: Context): HttpProxyCacheServer {
        return HttpProxyCacheServer(context.applicationContext)
    }

    private var proxy: HttpProxyCacheServer? = null
    private fun getProxy(context: Context): HttpProxyCacheServer {
        return proxy ?: newProxy(context).also { proxy = it }
    }

    @JvmStatic
    fun getCacheUrl(context: Context,url:String):String {
        try {
            return getProxy(context).getProxyUrl(url)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return url
    }
}
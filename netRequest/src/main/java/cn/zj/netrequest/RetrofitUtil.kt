package cn.zj.netrequest

import com.google.gson.GsonBuilder
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

class RetrofitUtil private constructor() {

    private val converterFactories = mutableListOf<Converter.Factory>()
    private val callAdapterFactories = mutableListOf<CallAdapter.Factory>()
    private var eventListener: EventListener.Factory? = null

    private var debugMode = true
    private var canProxy = true
    private var timeout = 15L
    private val intercepters = mutableListOf<Interceptor>()
    private val networkIntercepters = mutableListOf<Interceptor>()
    var retrofit: Retrofit? = null
        get() {
            if (field == null) {
                throw IllegalArgumentException("please init Retrofit first!")
            }
            return field
        }

    companion object {
        private var mInstance: RetrofitUtil? = null

        @Synchronized
        fun getInstance(): RetrofitUtil {
            if (mInstance == null) mInstance = RetrofitUtil()
            return mInstance as RetrofitUtil
        }

    }

    private var clientBuilder: OkHttpClient.Builder? = null

    private var retrofitBuilder: Retrofit.Builder? = null

    /**
     * retrofit初始化
     */
    @Synchronized
    fun init(
        baseUrl: String,
        okHttpClientExtraConfig: (OkHttpClient.Builder) -> Unit = {},
        retrofitClientExtraConfig: (Retrofit.Builder) -> Unit = {}
    ) {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        if (debugMode) {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        if (clientBuilder == null) {
            clientBuilder = OkHttpClient.Builder()
            if (!canProxy) clientBuilder?.proxy(Proxy.NO_PROXY)
            eventListener?.let {
                clientBuilder?.eventListenerFactory(it)
            }
            clientBuilder?.connectTimeout(timeout, TimeUnit.SECONDS)
                ?.writeTimeout(timeout, TimeUnit.SECONDS)
                ?.readTimeout(timeout, TimeUnit.SECONDS)?.addInterceptor(httpLoggingInterceptor)

            if (intercepters.isNotEmpty()) {
                for (intercepter in intercepters) {
                    clientBuilder?.addInterceptor(intercepter)
                }
            }
            if (networkIntercepters.isNotEmpty()) {
                for (intercepter in networkIntercepters) {
                    clientBuilder?.addNetworkInterceptor(intercepter)
                }
            }
            okHttpClientExtraConfig.invoke(clientBuilder!!)
        }
        if (retrofitBuilder == null) {
            retrofitBuilder = Retrofit.Builder()
            retrofitBuilder?.client(clientBuilder!!.build())?.baseUrl(baseUrl)
            if (converterFactories.size > 0) {
                converterFactories.forEach {
                    retrofitBuilder?.addConverterFactory(it)
                }
            } else {
                retrofitBuilder?.addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setLenient()
                            .create()
                    )
                )
            }
            if (callAdapterFactories.size > 0) {
                callAdapterFactories.forEach {
                    retrofitBuilder?.addCallAdapterFactory(it)
                }
            }
            retrofitClientExtraConfig.invoke(retrofitBuilder!!)
        } else {
            retrofitBuilder?.client(clientBuilder!!.build())?.baseUrl(baseUrl)
        }
        retrofit = retrofitBuilder?.build()
    }

    /**
     * 日志开关（默认debug打印日志，release关闭日志）
     */
    fun debugMode(isDebug: Boolean): RetrofitUtil {
        debugMode = isDebug
        return this
    }

    /**
     * 代理开关（默认允许抓包）
     */
    fun proxy(canProxy: Boolean): RetrofitUtil {
        this.canProxy = canProxy
        return this
    }

    /**
     * 超时时间(秒)(包含connectTimeout，writeTimeout，readTimeout)
     */
    fun timeout(timeoutSec: Long): RetrofitUtil {
        timeout = timeoutSec
        return this
    }


    /**
     * 拦截器集合(多个)
     */
    fun interceptors(interceptorList: List<Interceptor>): RetrofitUtil {
        intercepters.addAll(interceptorList)
        return this
    }

    /**
     * 拦截器集合(单个)
     */
    fun interceptor(interceptor: Interceptor): RetrofitUtil {
        intercepters.add(interceptor)
        return this
    }

    /**
     * 网络拦截器集合(多个)
     */
    fun networkInterceptors(interceptorList: List<Interceptor>): RetrofitUtil {
        networkIntercepters.addAll(interceptorList)
        return this
    }

    /**
     * 网络拦截器集合(单个)
     */
    fun networkInterceptor(interceptor: Interceptor): RetrofitUtil {
        networkIntercepters.add(interceptor)
        return this
    }


    /**
     * 转化器
     */
    fun converterFactory(converterFactory: Converter.Factory): RetrofitUtil {
        converterFactories.add(converterFactory)
        return this
    }


    /**
     * 适配器工厂
     */
    fun callAdapterFactory(callAdapterFactory: CallAdapter.Factory): RetrofitUtil {
        callAdapterFactories.add(callAdapterFactory)
        return this
    }

    /**
     * 适配器工厂
     */
    fun eventListenerFactory(eventListener: EventListener.Factory): RetrofitUtil {
        this.eventListener = eventListener
        return this
    }


}